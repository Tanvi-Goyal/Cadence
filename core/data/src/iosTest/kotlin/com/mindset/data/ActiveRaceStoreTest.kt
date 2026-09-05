@file:OptIn(ExperimentalTime::class)

package com.mindset.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mindset.datastore.createPreferencesDataStore
import com.mindset.domain.WeightUnit
import com.mindset.domain.repository.ActiveRaceSnapshot
import com.mindset.model.Gender
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The resume record's storage. Same harness as [PreferencesRepositoryTest] — a fresh unique temp file
 * per test, so cases don't share state or trip DataStore's one-instance-per-file guard.
 */
class ActiveRaceStoreTest {
    private fun newDataStore(): DataStore<Preferences> = createPreferencesDataStore(
        NSTemporaryDirectory() + "race-${NSUUID().UUIDString}.preferences_pb",
    )

    private fun snapshot(
        sessionId: String = "s1",
        currentIndex: Int = 3,
        totalElapsedMs: Long = 90_000,
        splitElapsedMs: Long = 12_000,
        paused: Boolean = false,
        snapshotAt: Long = 1_700_000_000_000,
    ) = ActiveRaceSnapshot(
        sessionId = sessionId,
        divisionKey = "MEN",
        variant = HyroxVariant.FULL,
        raceMode = RaceMode.SINGLES,
        gender = Gender.MEN,
        currentIndex = currentIndex,
        totalElapsedMs = totalElapsedMs,
        splitElapsedMs = splitElapsedMs,
        paused = paused,
        snapshotAt = Instant.fromEpochMilliseconds(snapshotAt),
    )

    @Test
    fun read_returnsNullWhenNothingWritten() = runTest {
        assertNull(ActiveRaceStoreImpl(newDataStore()).read())
    }

    @Test
    fun save_thenRead_roundTripsEveryField() = runTest {
        val store = ActiveRaceStoreImpl(newDataStore())
        val original = snapshot()

        store.save(original)

        assertEquals(original, store.read(), "every field survives the round trip")
    }

    @Test
    fun save_overwritesTheWholeRecord() = runTest {
        val store = ActiveRaceStoreImpl(newDataStore())

        store.save(snapshot(sessionId = "first", currentIndex = 1, totalElapsedMs = 1_000))
        store.save(snapshot(sessionId = "second", currentIndex = 9, totalElapsedMs = 500_000))

        val read = store.read()
        assertEquals("second", read?.sessionId, "no stale field bleeds through")
        assertEquals(9, read?.currentIndex)
        assertEquals(500_000, read?.totalElapsedMs)
    }

    /**
     * The record's fields are only meaningful together, so a partial one must read as absent rather
     * than as a plausible-looking half-built race.
     */
    @Test
    fun read_returnsNullWhenTheRecordIsIncomplete() = runTest {
        val dataStore = newDataStore()
        val store = ActiveRaceStoreImpl(dataStore)
        store.save(snapshot())

        dataStore.edit { it.remove(stringPreferencesKey("active_race_division_key")) }

        assertNull(store.read(), "a missing field makes the whole record unusable")
    }

    /** A stored enum this build no longer defines must not crash the restore path. */
    @Test
    fun read_returnsNullOnUnparseableEnum() = runTest {
        val dataStore = newDataStore()
        val store = ActiveRaceStoreImpl(dataStore)
        store.save(snapshot())

        dataStore.edit { it[stringPreferencesKey("active_race_variant")] = "TRIPLE_HALF" }

        assertNull(store.read())
    }

    /**
     * The race record shares the athlete's preferences file, so clearing it must not take their
     * settings with it.
     */
    @Test
    fun clear_leavesOtherPreferencesIntact() = runTest {
        val dataStore = newDataStore()
        val store = ActiveRaceStoreImpl(dataStore)
        val preferences = PreferencesRepositoryImpl(dataStore)

        preferences.setWeightUnit(WeightUnit.LB)
        store.save(snapshot())
        store.clear()

        assertNull(store.read(), "the race record is gone")
        assertEquals(
            WeightUnit.LB,
            preferences.observe().first().weightUnit,
            "the athlete's settings survive in the shared store",
        )
    }
}
