@file:OptIn(ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mindset.data

import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.ActiveRaceSnapshot
import com.mindset.domain.repository.ActiveRaceStore
import com.mindset.domain.repository.PreferencesRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.ActiveWorkout
import com.mindset.model.Gender
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [ActiveWorkoutController] backed by [SessionRepository]. Elapsed time is derived from the injected
 * [clock] (drift-free), not by counting ticks.
 *
 * **Concurrency:** the UI calls [pause]/[resume]/[next]/[reset] on the main thread while a tick loop
 * runs in the background. Rather than lock the mutable timing anchors, every operation and the tick
 * itself is dispatched onto a **single-threaded** scope ([Dispatchers.Default.limitedParallelism]`(1)`),
 * so they serialize and the plain `var` anchors are never touched concurrently.
 */
@OptIn(ExperimentalTime::class)
class ActiveWorkoutControllerImpl(
    private val repository: SessionRepository,
    private val preferences: PreferencesRepository,
    private val store: ActiveRaceStore,
    private val clock: Clock,
) : ActiveWorkoutController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))

    private val _state = MutableStateFlow<ActiveWorkout?>(null)
    override val state: StateFlow<ActiveWorkout?> = _state.asStateFlow()

    // Presentation-only: true = full sheet shown, false = minimized to the Home mini-card. Independent
    // of the timing anchors, so it never affects the clock/splits/persistence.
    private val _expanded = MutableStateFlow(true)
    override val expanded: StateFlow<Boolean> = _expanded.asStateFlow()

    // Mutable timing anchors — only ever touched on the single-threaded [scope].
    private var sessionId: String = ""
    private var divisionKey: String = ""
    private var variant: HyroxVariant = HyroxVariant.FULL

    // Snapshotted at start so a restore rebuilds the SAME step list even if the athlete changed
    // division or race mode in Profile meanwhile. (hyroxFormat ignores `gender` today; kept so the
    // rebuild call is byte-identical and forward-proof.)
    private var raceMode: RaceMode = RaceMode.SINGLES
    private var gender: Gender = Gender.WOMEN
    private var steps: List<HyroxStationModel> = emptyList()
    private var currentIndex = 0
    private var totalAccumMs = 0L // total elapsed banked at the last pause/advance
    private var splitAccumMs = 0L // current-split elapsed banked at the last pause
    private var runStartMark: Instant = Instant.DISTANT_PAST
    private var paused = false
    private var finished = false
    private var tickJob: Job? = null

    // Guards the async gap inside startHyrox. [scope] is limitedParallelism(1), which serializes
    // DISPATCH but not a coroutine across its suspension points — so two quick Start taps would both
    // pass a `steps.isEmpty()` check while the first awaits the DB, creating two sessions and
    // orphaning the first. Set before the first suspension point; only ever touched on [scope].
    private var starting = false

    // First-call-wins guard for [restore]; the foreground service may also call it.
    private var restoreAttempted = false

    // When the heartbeat last wrote, so the tick loop can throttle to one write per HEARTBEAT_MS.
    private var lastPersistMark: Instant = Instant.DISTANT_PAST

    override fun startHyrox(divisionKey: String, variant: HyroxVariant, templateId: String) = onScope {
        if (starting || steps.isNotEmpty()) {
            _expanded.value = true
            return@onScope
        }

        starting = true

        try {
            // Mode + gender come from the athlete's profile. Note hyroxFormat ignores `gender` today
            // (it keys standards off divisionKey + raceMode), so an overridden division cannot desync
            // from the profile gender — the parameter is forward-looking API only.
            val raceInfo = preferences.getRaceInfo()
            val raceMode = raceInfo.third ?: RaceMode.SINGLES
            val gender = raceInfo.second
                ?: if (divisionKey.startsWith("WOMEN")) Gender.WOMEN else Gender.MEN

            val format = repository.hyroxFormat(divisionKey, variant, raceMode, gender)
            if (format.isEmpty()) return@onScope

            val id = repository.startHyroxSession(divisionKey, variant, raceMode, gender, templateId)

            sessionId = id
            this@ActiveWorkoutControllerImpl.divisionKey = divisionKey
            this@ActiveWorkoutControllerImpl.variant = variant
            this@ActiveWorkoutControllerImpl.raceMode = raceMode
            this@ActiveWorkoutControllerImpl.gender = gender
            steps = format
            currentIndex = 0
            totalAccumMs = 0L
            splitAccumMs = 0L
            runStartMark = clock.now()
            paused = false
            finished = false
            _expanded.value = true // a fresh workout opens the full sheet
            emit()
            persist()
            startTicking()
        } finally {
            starting = false
        }
    }

    override fun restore() = onScope {
        if (restoreAttempted) return@onScope
        restoreAttempted = true
        // A race is already live (or being started): never clobber it — and never clear its record.
        if (starting || steps.isNotEmpty()) return@onScope

        // Same guard startHyrox needs: the scope serializes DISPATCH, not a coroutine across its
        // suspension points, and everything below suspends. Without this a Start tap could begin
        // underneath an in-flight restore.
        starting = true
        try {
            val record = store.read() ?: return@onScope

            // Stale: a race is 60-90 minutes, so anything from another day is abandoned rather than
            // paused. Measured from when the app last OBSERVED the race, not from startedAt — a race
            // legitimately paused for hours at the gym must survive. Splits already written stay in
            // History either way; only the resume hint is dropped.
            if (clock.now() - record.snapshotAt > STALE_AFTER) {
                store.clear()
                return@onScope
            }

            // The DB is the authority on whether this race is still resumable. This check is what
            // makes it safe not to persist a `finished` flag: a crash between finishSession() and
            // store.clear() leaves a record for a completed race, and this catches it.
            val detail = repository.observeSessionDetail(record.sessionId).first()
            val session = detail?.session
            if (session == null || session.finishedAt != null || session.deletedAt != null) {
                store.clear()
                return@onScope
            }

            val format = repository.hyroxFormat(
                record.divisionKey,
                record.variant,
                record.raceMode,
                record.gender,
            )
            if (format.isEmpty()) {
                store.clear()
                return@onScope
            }

            sessionId = record.sessionId
            divisionKey = record.divisionKey
            variant = record.variant
            raceMode = record.raceMode
            gender = record.gender
            steps = format
            currentIndex = record.currentIndex.coerceIn(0, format.lastIndex)
            totalAccumMs = record.totalElapsedMs
            splitAccumMs = record.splitElapsedMs
            // Always paused, whatever the record said: while the process was dead nobody observed the
            // clock, so counting that wall time would inflate the race and write an inflated total to
            // History and the Station board. The athlete opts back in by tapping Resume.
            paused = true
            finished = false
            // Unreachable while paused (liveMs() short-circuits to 0) and resume() re-anchors it
            // anyway — but every other assignment site maintains this invariant, and DISTANT_PAST
            // would yield a nonsense elapsed if that short-circuit were ever removed.
            runStartMark = clock.now()
            // Restored MINIMIZED. A rehydrated race arrives on cold start, so expanding here would
            // throw the full sheet over Home before the athlete has asked for anything. The toolbar
            // pill (and Home's "In progress" card) carry it; both reopen the sheet on tap, and both
            // already show the paused clock, so the Resume-or-Dismiss decision is still right there.
            _expanded.value = false

            // Deliberately no startTicking(): the race is paused, so a tick loop would spin as a no-op
            // for as long as it stays paused. resume() starts it.
            emit()
            // Deliberately no persist(): the stored record is already correct, and rewriting it would
            // push snapshotAt forward and defeat the staleness cutoff if the athlete never resumes.
        } finally {
            starting = false
        }
    }

    override fun pause() = onScope {
        if (paused || finished || steps.isEmpty()) return@onScope
        val live = liveMs()
        totalAccumMs += live
        splitAccumMs += live
        paused = true
        emit()
        persist()
    }

    override fun resume() = onScope {
        if (!paused || finished || steps.isEmpty()) return@onScope
        runStartMark = clock.now()
        paused = false
        emit()
        persist()
        // Required, not belt-and-braces: a restored race has no tick job (restore deliberately leaves
        // a paused race idle), so without this the clock would only move when the athlete taps Next.
        // startTicking() cancels-then-relaunches, so it is safe on the normal resume path too.
        startTicking()
    }

    override fun next() = onScope {
        if (finished || steps.isEmpty()) return@onScope
        // Bank the live portion, persist this step's split, then reset the split clock.
        val live = liveMs()
        totalAccumMs += live
        splitAccumMs += live
        repository.recordHyroxSplit(sessionId, currentIndex, (splitAccumMs / 1000).toInt())
        splitAccumMs = 0L
        if (!paused) runStartMark = clock.now()

        if (currentIndex >= steps.lastIndex) {
            finished = true
            repository.finishSession(sessionId)
            tickJob?.cancel()
        } else {
            currentIndex++
        }
        emit()
        // Persisted AFTER recordHyroxSplit above, deliberately. A crash in that gap leaves the index
        // on a step whose split is already written — the athlete re-runs it and next() overwrites,
        // which is visible and self-correcting. The reverse order would leave a HOLE in History with
        // PB detection skipped for that station: silent data loss.
        if (finished) store.clear() else persist()
    }

    override fun reset() = onScope {
        if (steps.isEmpty()) return@onScope
        currentIndex = 0
        totalAccumMs = 0L
        splitAccumMs = 0L
        runStartMark = clock.now()
        paused = false
        finished = false
        startTicking()
        emit()
        persist()
    }

    override fun collapse() = onScope { _expanded.value = false }

    override fun expand() = onScope { _expanded.value = true }

    override fun dismiss() = onScope {
        tickJob?.cancel()
        tickJob = null
        steps = emptyList()
        _state.value = null
        // Required: dismiss() deliberately leaves the other anchors stale (startHyrox overwrites them
        // wholesale), so without this clear the dismissed race would resurrect on the next cold start.
        store.clear()
    }

    private fun onScope(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob =
            scope.launch {
                while (isActive) {
                    delay(TICK_MS)
                    if (!paused && !finished && steps.isNotEmpty()) {
                        emit()
                        heartbeat()
                    }
                }
            }
    }

    /**
     * Write the live-inclusive snapshot. Called at the six points that actually mutate an anchor —
     * NOT from [emit], which runs 5x/sec and changes nothing but derived values.
     */
    private suspend fun persist() {
        if (steps.isEmpty()) return
        val live = liveMs()
        store.save(
            ActiveRaceSnapshot(
                sessionId = sessionId,
                divisionKey = divisionKey,
                variant = variant,
                raceMode = raceMode,
                gender = gender,
                currentIndex = currentIndex,
                totalElapsedMs = totalAccumMs + live,
                splitElapsedMs = splitAccumMs + live,
                paused = paused,
                snapshotAt = clock.now(),
            ),
        )
        lastPersistMark = clock.now()
    }

    /**
     * Re-persist the running elapsed at most once per [HEARTBEAT_MS].
     *
     * Without it a restore would rewind to the last banked checkpoint — potentially a whole station.
     * Counting `now - runStartMark` instead would count time nobody observed, which is exactly what
     * restoring paused exists to avoid. This bounds the loss to the heartbeat period instead.
     *
     * Runs only while unpaused, so a race left paused overnight does no I/O at all.
     */
    private suspend fun heartbeat() {
        if ((clock.now() - lastPersistMark) < HEARTBEAT_INTERVAL) return
        persist()
    }

    private fun liveMs(): Long = if (paused) 0L else (clock.now() - runStartMark).inWholeMilliseconds

    private fun emit() {
        if (steps.isEmpty()) return
        val live = liveMs()
        _state.value =
            ActiveWorkout(
                sessionId = sessionId,
                divisionKey = divisionKey,
                variant = variant,
                steps = steps,
                currentIndex = currentIndex,
                totalElapsedMs = totalAccumMs + live,
                splitElapsedMs = splitAccumMs + live,
                paused = paused,
                finished = finished,
            )
    }

    private companion object {
        const val TICK_MS = 200L

        /**
         * Heartbeat period. 10s bounds the worst-case loss on process death to 10s of a ~90min race
         * (~0.2%) for ~540 small writes. 30s would halve the I/O but a station split is often only
         * three minutes, where a 30s error is material; 5s doubles it for an error already below the
         * noise of a manual Next tap.
         */
        val HEARTBEAT_INTERVAL: Duration = 10.seconds

        /** A race is 60-90 minutes; a record older than this is abandoned, not paused. */
        val STALE_AFTER: Duration = 12.hours
    }
}
