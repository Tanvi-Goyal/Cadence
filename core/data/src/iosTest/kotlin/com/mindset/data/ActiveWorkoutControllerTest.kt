@file:OptIn(ExperimentalTime::class)

package com.mindset.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mindset.common.UuidV7Generator
import com.mindset.data.local.AppDatabase
import com.mindset.datastore.createPreferencesDataStore
import com.mindset.domain.repository.ActiveRaceSnapshot
import com.mindset.domain.repository.ActiveRaceStore
import com.mindset.model.ActiveWorkout
import com.mindset.model.Gender
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The live-race timer: persistence at every anchor mutation, and rebuilding a race that outlived the
 * process.
 *
 * Uses the REAL [SessionRepositoryImpl] over an in-memory Room DB (the module convention, see
 * [SessionRepositoryTest]) so `hyroxFormat`/`startHyroxSession` are genuinely exercised.
 *
 * **Timing strategy.** The controller runs on its production dispatcher and the tests await observable
 * conditions — deliberately NOT a `TestDispatcher` with virtual time. The tick loop is an infinite
 * `delay(200)`, so under virtual time `runTest` advances the clock forever waiting for real Room I/O
 * that virtual time can never satisfy, and the suite deadlocks. Elapsed time is instead controlled
 * through [MutableClock], which is what the controller actually derives every duration from.
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

    /** In-memory [ActiveRaceStore] that counts writes, so the heartbeat throttle is assertable. */
    private class RecordingStore : ActiveRaceStore {
        var snapshot: ActiveRaceSnapshot? = null
        var saves = 0

        override suspend fun read(): ActiveRaceSnapshot? = snapshot

        override suspend fun save(snapshot: ActiveRaceSnapshot) {
            this.snapshot = snapshot
            saves++
        }

        override suspend fun clear() {
            snapshot = null
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

    private fun repository() = SessionRepositoryImpl(
        database,
        emptyExerciseAssetReader,
        UuidV7Generator(Clock.System),
        Clock.System,
    )

    /** Race mode/gender are read from preferences at start; use the real impl over a temp store. */
    private suspend fun preferences(): PreferencesRepositoryImpl {
        val prefs = PreferencesRepositoryImpl(
            createPreferencesDataStore(
                NSTemporaryDirectory() + "ctrl-${NSUUID().UUIDString}.preferences_pb",
            ),
        )
        prefs.setRaceInfo("HYROX", Gender.MEN, "MEN", RaceMode.SINGLES)
        return prefs
    }

    private fun controller(clock: Clock, store: ActiveRaceStore, preferences: PreferencesRepositoryImpl) =
        ActiveWorkoutControllerImpl(repository(), preferences, store, clock)

    /**
     * Await an observable effect. A controller call is NOT complete when the test body regains
     * control — the repository does real Room I/O on its own dispatchers — so assertions have to wait
     * for the state the call produces.
     */
    private suspend fun ActiveWorkoutControllerImpl.awaitState(
        predicate: (ActiveWorkout) -> Boolean = { true },
    ): ActiveWorkout = state.filterNotNull().first(predicate)

    /** Real (not virtual) elapsed time — `delay` inside `runTest` would return instantly. */
    private suspend fun realDelay(millis: Long) = withContext(Dispatchers.Default) { delay(millis) }

    /**
     * Drop the in-memory race the way process death does, keeping what was on disk. `dismiss()` also
     * stops the abandoned controller's tick loop, which would otherwise run for the rest of the suite.
     */
    private suspend fun ActiveWorkoutControllerImpl.simulateProcessDeath(store: RecordingStore) {
        val onDisk = store.snapshot
        dismiss()
        realDelay(50)
        store.snapshot = onDisk
    }

    // ── persistence ────────────────────────────────────────────────────────────────────────────

    @Test
    fun startHyrox_persistsTheAnchors() = runTest {
        val store = RecordingStore()
        val timer = controller(MutableClock(Instant.fromEpochMilliseconds(1_000_000)), store, preferences())

        timer.startHyrox("MEN", HyroxVariant.FULL, "full-hyrox-simulation")
        timer.awaitState()

        val saved = assertNotNull(store.snapshot, "a started race is persisted immediately")
        assertEquals(0, saved.currentIndex)
        assertEquals(0L, saved.totalElapsedMs)
        assertEquals(RaceMode.SINGLES, saved.raceMode, "the mode the race actually started with")
        assertTrue(!saved.paused)
        timer.dismiss()
    }

    @Test
    fun pause_persistsTheBankedElapsed() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences())

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        clock += 90.seconds
        timer.pause()
        timer.awaitState { it.paused }

        val saved = assertNotNull(store.snapshot)
        assertEquals(90_000L, saved.totalElapsedMs, "elapsed is banked at the pause")
        assertTrue(saved.paused)
        timer.dismiss()
    }

    /**
     * The tick runs at 5Hz but mutates nothing, so persistence must be throttled. Holding the clock
     * still means the heartbeat's interval never elapses: many ticks, zero writes. Without the
     * throttle this would be one write per tick.
     */
    @Test
    fun tick_doesNotPersistOnEveryTick() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences())

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        val afterStart = store.saves

        realDelay(1_200) // ~6 ticks at 200ms, with the wall clock held still

        assertEquals(afterStart, store.saves, "ticks alone must not write — they mutate nothing")

        // Move past the heartbeat interval: exactly one catch-up write, not one per tick.
        clock += 11.seconds
        realDelay(600)
        assertEquals(afterStart + 1, store.saves, "one heartbeat, not one per tick")
        timer.dismiss()
    }

    @Test
    fun pausedRace_doesNotHeartbeat() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences())

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        timer.pause()
        timer.awaitState { it.paused }
        val afterPause = store.saves

        clock += 1.hours
        realDelay(800)

        assertEquals(afterPause, store.saves, "a paused race does no I/O at all")
        timer.dismiss()
    }

    @Test
    fun dismiss_clearsTheRecord() = runTest {
        val store = RecordingStore()
        val timer = controller(MutableClock(Instant.fromEpochMilliseconds(1_000_000)), store, preferences())

        timer.startHyrox("MEN", HyroxVariant.FULL, "t")
        timer.awaitState()
        timer.dismiss()
        realDelay(100)

        assertNull(store.snapshot, "a dismissed race must not resurrect on the next cold start")
    }

    @Test
    fun finishing_clearsTheRecord() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val timer = controller(clock, store, preferences())

        timer.startHyrox("MEN", HyroxVariant.FIRST_HALF, "t")
        val started = timer.awaitState()
        repeat(started.totalSteps) { step ->
            clock += 30.seconds
            timer.next()
            timer.awaitState { it.finished || it.currentIndex > step }
        }
        realDelay(100)

        assertTrue(assertNotNull(timer.state.value).finished, "the race finished")
        assertNull(store.snapshot, "a finished race must not resurrect")
    }

    // ── restore ────────────────────────────────────────────────────────────────────────────────

    @Test
    fun restore_withNoRecord_leavesStateNull() = runTest {
        val timer = controller(MutableClock(Instant.fromEpochMilliseconds(1_000_000)), RecordingStore(), preferences())

        timer.restore()
        realDelay(200)

        assertNull(timer.state.value)
    }

    /**
     * The money test: a race is persisted, the process dies, the app reopens two hours later. The
     * restored total must be exactly what was last observed — wall time that passed while nothing was
     * watching the clock is not race time.
     */
    @Test
    fun restore_doesNotCountTimeWhileTheProcessWasDead() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()

        val first = controller(clock, store, prefs)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        clock += 90.seconds
        first.pause() // banks 90s and persists
        first.awaitState { it.paused }
        first.simulateProcessDeath(store)

        clock += 2.hours
        val revived = controller(clock, store, prefs)
        revived.restore()
        val restored = revived.awaitState()

        assertEquals(90_000L, restored.totalElapsedMs, "exactly the observed elapsed — not +2h")
        assertTrue(restored.paused, "restored races land paused for an explicit Resume")
        assertEquals(16, restored.totalSteps, "steps were rebuilt from the seeded format")
        revived.dismiss()
    }

    /**
     * `resume()` must (re)start the tick loop. A restored controller has no tick job, so without that
     * the clock sits frozen and only moves when the athlete taps Next.
     */
    @Test
    fun restore_thenResume_advancesTheClock() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()

        val first = controller(clock, store, prefs)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        clock += 90.seconds
        first.pause()
        first.awaitState { it.paused }
        first.simulateProcessDeath(store)

        val revived = controller(clock, store, prefs)
        revived.restore()
        revived.awaitState()
        revived.resume()
        revived.awaitState { !it.paused }

        clock += 10.seconds
        val ticked = revived.awaitState { it.totalElapsedMs >= 100_000 }

        assertEquals(100_000L, ticked.totalElapsedMs, "resume continues from the restored elapsed")
        revived.dismiss()
    }

    @Test
    fun restore_ofAStaleRecord_clearsAndDoesNotRestore() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()

        val first = controller(clock, store, prefs)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        first.pause()
        first.awaitState { it.paused }
        first.simulateProcessDeath(store)

        clock += 13.hours // past the same-day cutoff
        val revived = controller(clock, store, prefs)
        revived.restore()
        realDelay(300)

        assertNull(revived.state.value, "an abandoned race is not resumed")
        assertNull(store.snapshot, "and its resume hint is dropped")
    }

    /** Restore must never clobber a race the athlete just started — nor clear its fresh record. */
    @Test
    fun restore_afterStart_keepsTheLiveRaceAndItsRecord() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()

        val first = controller(clock, store, prefs)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        first.pause()
        first.awaitState { it.paused }
        first.simulateProcessDeath(store)

        val revived = controller(clock, store, prefs)
        revived.startHyrox("MEN", HyroxVariant.FIRST_HALF, "t2")
        val live = revived.awaitState()
        revived.restore()
        realDelay(300)

        assertEquals(8, assertNotNull(revived.state.value).totalSteps, "the fresh race is untouched")
        val record = assertNotNull(store.snapshot, "its record was NOT cleared by the restore bail-out")
        assertEquals(live.sessionId, record.sessionId, "the record tracks the freshly started race")
        revived.dismiss()
    }

    @Test
    fun restore_isIdempotent() = runTest {
        val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000))
        val store = RecordingStore()
        val prefs = preferences()

        val first = controller(clock, store, prefs)
        first.startHyrox("MEN", HyroxVariant.FULL, "t")
        first.awaitState()
        first.pause()
        first.awaitState { it.paused }
        first.simulateProcessDeath(store)

        val revived = controller(clock, store, prefs)
        revived.restore()
        val afterFirst = revived.awaitState()
        revived.restore()
        realDelay(200)

        assertEquals(afterFirst, revived.state.value, "a second restore is a no-op")
        revived.dismiss()
    }
}
