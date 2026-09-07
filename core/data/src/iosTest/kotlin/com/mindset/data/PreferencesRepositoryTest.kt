package com.mindset.data

import com.mindset.datastore.createPreferencesDataStore
import com.mindset.domain.ThemeMode
import com.mindset.domain.WeightUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Preferences persistence over a DataStore. Each test gets a fresh, unique temp file so cases don't
 * share state and don't trip DataStore's "multiple instances for the same file" guard. Lives in
 * iosTest alongside the other native persistence tests.
 */
class PreferencesRepositoryTest {
    private fun newRepository(): PreferencesRepositoryImpl = PreferencesRepositoryImpl(
        createPreferencesDataStore(
            NSTemporaryDirectory() + "prefs-${NSUUID().UUIDString}.preferences_pb",
        ),
    )

    @Test
    fun observe_defaultsWhenNothingWritten() = runTest {
        val prefs = newRepository().observe().first()
        assertEquals(WeightUnit.KG, prefs.weightUnit, "default unit is kg")
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode, "default theme follows system")
        assertFalse(prefs.isOnboardingComplete, "onboarding is not complete by default")
    }

    @Test
    fun setters_roundTripThroughDataStore() = runTest {
        val repository = newRepository()

        repository.setWeightUnit(WeightUnit.LB)
        repository.setThemeMode(ThemeMode.DARK)
        repository.setOnboardingComplete(true)

        val prefs = repository.observe().first()
        assertEquals(WeightUnit.LB, prefs.weightUnit)
        assertEquals(ThemeMode.DARK, prefs.themeMode)
        assertTrue(prefs.isOnboardingComplete)
    }
}
