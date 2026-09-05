@file:OptIn(ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mindset.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mindset.common.UuidV7Generator
import com.mindset.data.local.AppDatabase
import com.mindset.datastore.createPreferencesDataStore
import com.mindset.domain.repository.ActiveRaceSnapshot
import com.mindset.domain.repository.ActiveRaceStore
import com.mindset.model.Gender
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The live-race timer: persistence at every anchor mutation, and rebuilding a race that outlived the
 * process. Uses the REAL [SessionRepositoryImpl] over an in-memory Room DB (the module's convention,
 * see [SessionRepositoryTest]) so `hyroxFormat`/`startHyroxSession` are genuinely exercised rather
 * than faked, and a [MutableClock] + [StandardTestDispatcher] so the 200ms tick loop is deterministic.
 */
class ActiveWorkoutControllerTest {

    private lateinit var database: AppDatabase

    /** Wall clock under test control — the controller derives all elapsed time from this. */
    private class MutableClock(var instant: Instant) : Clock {
        override fun now(): Instant = instant
        operator fun plusAssign(duration: Duration) {
            instant += duration
        }
    }

    /** In-memory [ActiveRaceStore] that also counts writes, so the heartbeat rate is assertable. */
    private class RecordingStore : ActiveRaceStore {
        var snapshot: ActiveRaceSnapshot? = null
        var saves = 0
        var clears = 0

        override suspend fun read(): ActiveRaceSnapshot? = snapshot
        override suspend fun save(snapshot: ActiveRaceSnapshot) {
            this.snapshot = snapshot
            saves++
        }

        override suspend fun clear() {
            snapshot = null
            clears++
        }
    }

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun repository() = SessionRepositoryImpl(
        database,
        emptyExerciseAssetReader,
        UuidV7Generator(Clock.System),
        Clock.System,
    )

    /** Preferences supply race mode/gender at start; the real impl over a throwaway DataStore. */
    private suspend fun preferences(): PreferencesRepositoryImpl {
        val prefs = PreferencesRepositoryImpl(
            createPreferencesDataStore(
                NSTemporaryDirectory() + "ctrl-${NSUUID().UUIDString}.preferences_pb",
            ),
        )
        prefs.setRaceInfo("HYROX", Gender.MEN, "MEN", RaceMode.SINGLES)
        return prefs
    }

    private fun controller(
        clock: MutableClock,
        store: ActiveRaceStore,
        preferences: PreferencesRepositoryImpl,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    ) = ActiveWorkoutControllerImpl(repository(), preferences, store, clock, dispatcher)

    /**
     * Await an observable effect. The repository does real Room I/O on its own dispatchers, outside
     * the test scheduler, so a controller call is NOT complete when the test body regains control —
     * assertions have to wait for the state the call produces.
     */
    private suspend fun ActiveWorkoutControllerImpl.awaitState(
        predicate: (com.mindset.model.ActiveWorkout) -> Boolean = { true },
    ): com.mindset.model.ActiveWorkout = state.filterNotNull().first(predicate)

    // ── persistence ────────────────────────────────────────────────────────────────────────────

    @Test
    fun startHyrox_persistsTheAnchors() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.startHyrox("MEN", HyroxVariant.FULL, "full-hyrox-simulation")
        timer.awaitState()
        advanceUntilIdle()

        val saved = assertNotNull(store.snapshot, "a started race is persisted immediately")
        assertEquals(0, saved.currentIndex)
        assertEquals(0L, saved.totalElapsedMs)
        assertEquals(RaceMode.SINGLES, saved.raceMode, "the mode the race actually started with")
        assertTrue(!saved.paused)
        timer.dismiss()
        advanceUntilIdle()
    }

    @Test
    fun pause_persistsTheBankedElapsed() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        clock += 90.seconds
        timer.pause()
        timer.awaitState { it.paused }
        advanceUntilIdle()

        val saved = assertNotNull(store.snapshot)
        assertEquals(90_000L, saved.totalElapsedMs, "elapsed is banked at the pause")
        assertTrue(saved.paused)
        timer.dismiss()
        advanceUntilIdle()
    }

    /** The tick runs at 5Hz but mutates nothing, so persistence must be throttled, not per-tick. */
    @Test
    fun tick_heartbeatsAtMostOncePerTenSeconds() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        advanceUntilIdle()
        val afterStart = store.saves

        // 30 virtual seconds = 150 ticks. Advance the wall clock in step so the heartbeat can fire.
        repeat(150) {
            clock += 200.milliseconds
            advanceTimeBy(200)
        }

        val heartbeats = store.saves - afterStart
        assertTrue(
            heartbeats in 2..4,
            "30s of ticks should heartbeat ~3 times, not once per tick — was $heartbeats",
        )
        timer.dismiss()
        advanceUntilIdle()
    }

    @Test
    fun pausedRace_doesNotHeartbeat() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        timer.pause()
        timer.awaitState { it.paused }
        advanceUntilIdle()
        val afterPause = store.saves

        repeat(300) {
            clock += 200.milliseconds
            advanceTimeBy(200)
        }

        assertEquals(afterPause, store.saves, "a paused race does no I/O at all")
        timer.dismiss()
        advanceUntilIdle()
    }

    @Test
    fun dismiss_clearsTheRecord() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        timer.dismiss()
        advanceUntilIdle()

        assertNull(store.snapshot, "a dismissed race must not resurrect on the next cold start")
    }

    @Test
    fun finishing_clearsTheRecord() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.startHyrox("MEN", HyroxVariant.FIRST_HALF, "t")
        val started = timer.awaitState()
        repeat(started.totalSteps) { step ->
            clock += 30.seconds
            timer.next()
            timer.awaitState { it.finished || it.currentIndex > step }
        }
        advanceUntilIdle()

        assertTrue(assertNotNull(timer.state.value).finished, "the race finished")
        assertNull(store.snapshot, "a finished race must not resurrect")
    }

    // ── restore ────────────────────────────────────────────────────────────────────────────────

    @Test
    fun restore_withNoRecord_leavesStateNull() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val timer = controller(clock, RecordingStore(), preferences(), UnconfinedTestDispatcher(testScheduler))

        timer.restore()
        advanceUntilIdle()

        assertNull(timer.state.value)
    }

    /**
     * The money test: a race persisted, then the process dies, then the app reopens two hours later.
     * The restored total must be exactly what was last observed — wall time that passed while nothing
     * was watching the clock is not race time.
     */
    @Test
    fun restore_doesNotCountTimeWhileTheProcessWasDead() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val first = controller(clock, store, prefs, dispatcher)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        clock += 90.seconds
        first.pause() // banks 90s and persists
        first.awaitState { it.paused }
        advanceUntilIdle()
        // Simulate process death: dismiss stops the abandoned controller's tick loop (an infinite
        // delay loop would otherwise keep the test scheduler busy forever). It also clears storage, so
        // put the record back — on a real process death it survives on disk.
        val onDisk = store.snapshot
        first.dismiss()
        advanceUntilIdle()
        store.snapshot = onDisk

        // Process dies; the app is reopened two hours later.
        clock += 2.hours
        val revived = controller(clock, store, prefs, dispatcher)
        revived.restore()
        val restored = revived.awaitState()
        advanceUntilIdle()
        assertEquals(90_000L, restored.totalElapsedMs, "exactly the observed elapsed — not +2h")
        assertTrue(restored.paused, "restored races land paused for an explicit Resume")
        assertEquals(16, restored.totalSteps, "steps were rebuilt from the seeded format")
        revived.dismiss()
        advanceUntilIdle()
    }

    /**
     * `resume()` must (re)start the tick loop. A restored controller has no tick job, so without that
     * the clock would sit frozen and only move when the athlete taps Next.
     */
    @Test
    fun restore_thenResume_advancesTheClock() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val first = controller(clock, store, prefs, dispatcher)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        clock += 90.seconds
        first.pause()
        first.awaitState { it.paused }
        advanceUntilIdle()
        // Simulate process death: dismiss stops the abandoned controller's tick loop (an infinite
        // delay loop would otherwise keep the test scheduler busy forever). It also clears storage, so
        // put the record back — on a real process death it survives on disk.
        val onDisk = store.snapshot
        first.dismiss()
        advanceUntilIdle()
        store.snapshot = onDisk

        val revived = controller(clock, store, prefs, dispatcher)
        revived.restore()
        revived.awaitState()
        revived.resume()
        revived.awaitState { !it.paused }

        // Ten seconds of wall time, ticked through.
        repeat(50) {
            clock += 200.milliseconds
            advanceTimeBy(200)
        }

        assertEquals(
            100_000L,
            assertNotNull(revived.state.value).totalElapsedMs,
            "resume continues from the restored elapsed and the clock actually ticks",
        )
        revived.dismiss()
        advanceUntilIdle()
    }

    @Test
    fun restore_ofAStaleRecord_clearsAndDoesNotRestore() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val first = controller(clock, store, prefs, dispatcher)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        first.pause()
        first.awaitState { it.paused }
        advanceUntilIdle()
        // Simulate process death: dismiss stops the abandoned controller's tick loop (an infinite
        // delay loop would otherwise keep the test scheduler busy forever). It also clears storage, so
        // put the record back — on a real process death it survives on disk.
        val onDisk = store.snapshot
        first.dismiss()
        advanceUntilIdle()
        store.snapshot = onDisk

        clock += 13.hours // past the same-day cutoff
        val revived = controller(clock, store, prefs, dispatcher)
        revived.restore()
        advanceUntilIdle()

        assertNull(revived.state.value, "an abandoned race is not resumed")
        assertNull(store.snapshot, "and its resume hint is dropped")
    }

    /** Restore must never clobber a race the athlete just started — nor clear its fresh record. */
    @Test
    fun restore_afterStart_keepsTheLiveRaceAndItsRecord() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        // A stale record exists from a previous process.
        val first = controller(clock, store, prefs, dispatcher)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        first.pause()
        first.awaitState { it.paused }
        advanceUntilIdle()
        // Simulate process death: dismiss stops the abandoned controller's tick loop (an infinite
        // delay loop would otherwise keep the test scheduler busy forever). It also clears storage, so
        // put the record back — on a real process death it survives on disk.
        val onDisk = store.snapshot
        first.dismiss()
        advanceUntilIdle()
        store.snapshot = onDisk

        val revived = controller(clock, store, prefs, dispatcher)
        revived.startHyrox("MEN", HyroxVariant.FIRST_HALF, "t2")
        val live = revived.awaitState()
        revived.restore()
        advanceUntilIdle()

        assertEquals(8, live.totalSteps, "the freshly started race is untouched")
        val record = assertNotNull(store.snapshot, "its record was NOT cleared by the restore bail-out")
        assertEquals(live.sessionId, record.sessionId, "the record tracks the freshly started race")
        revived.dismiss()
        advanceUntilIdle()
    }

    @Test
    fun restore_isIdempotent() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val first = controller(clock, store, prefs, dispatcher)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        first.pause()
        first.awaitState { it.paused }
        advanceUntilIdle()
        // Simulate process death: dismiss stops the abandoned controller's tick loop (an infinite
        // delay loop would otherwise keep the test scheduler busy forever). It also clears storage, so
        // put the record back — on a real process death it survives on disk.
        val onDisk = store.snapshot
        first.dismiss()
        advanceUntilIdle()
        store.snapshot = onDisk

        val revived = controller(clock, store, prefs, dispatcher)
        revived.restore()
        revived.awaitState()
        val afterFirst = revived.state.value
        revived.restore()
        advanceUntilIdle()

        assertEquals(afterFirst, revived.state.value, "a second restore is a no-op")
        revived.dismiss()
        advanceUntilIdle()
    }
}
