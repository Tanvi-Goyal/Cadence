@file:OptIn(ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mindset.data

import com.mindset.domain.ActiveWorkout
import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.PreferencesRepository
import com.mindset.domain.repository.SessionRepository
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
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

    override fun startHyrox(divisionKey: String, variant: HyroxVariant, templateId: String) = onScope {
        // Already racing (or mid-start): surface the running race instead of replacing it.
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
            if (format.isEmpty()) return@onScope // unseeded format — nothing to run

            val id = repository.startHyroxSession(divisionKey, variant, raceMode, gender, templateId)

            // Re-initialise EVERY anchor: dismiss() clears only `steps`/`_state`, so a second race
            // would otherwise inherit the first one's elapsed time and step index.
            sessionId = id
            this@ActiveWorkoutControllerImpl.divisionKey = divisionKey
            this@ActiveWorkoutControllerImpl.variant = variant
            steps = format
            currentIndex = 0
            totalAccumMs = 0L
            splitAccumMs = 0L
            runStartMark = clock.now()
            paused = false
            finished = false
            _expanded.value = true // a fresh workout opens the full sheet
            emit()
            startTicking()
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
    }

    override fun resume() = onScope {
        if (!paused || finished) return@onScope
        runStartMark = clock.now()
        paused = false
        emit()
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
    }

    override fun collapse() = onScope { _expanded.value = false }

    override fun expand() = onScope { _expanded.value = true }

    override fun dismiss() = onScope {
        tickJob?.cancel()
        tickJob = null
        steps = emptyList()
        _state.value = null
    }

    // ── internals ───────────────────────────────────────────────────────────────────────────────

    private fun onScope(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob =
            scope.launch {
                while (isActive) {
                    delay(TICK_MS)
                    if (!paused && !finished && steps.isNotEmpty()) emit()
                }
            }
    }

    /** Live milliseconds since the last resume/advance (0 while paused). */
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
    }
}
