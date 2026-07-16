package dev.cadence.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.cadence.data.local.AppDatabase
import dev.cadence.domain.ThemeMode
import dev.cadence.domain.WeightUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Preferences persistence: defaults when unset, and set round-trips through the DB. Lives in iosTest
 * for the same reason as [SessionRepositoryTest] — Room's no-arg in-memory builder is Context-free
 * on native.
 */
class PreferencesRepositoryTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @Test
    fun observe_defaultsWhenNoRowWritten() = runTest {
        val prefs = PreferencesRepositoryImpl(database).observe().first()
        assertEquals(WeightUnit.KG, prefs.weightUnit, "default unit is kg")
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode, "default theme follows system")
    }

    @Test
    fun setPreferences_roundTripsThroughDb() = runTest {
        val repository = PreferencesRepositoryImpl(database)

        repository.setWeightUnit(WeightUnit.LB)
        repository.setThemeMode(ThemeMode.DARK)

        val prefs = repository.observe().first()
        assertEquals(WeightUnit.LB, prefs.weightUnit)
        assertEquals(ThemeMode.DARK, prefs.themeMode)
    }
}
