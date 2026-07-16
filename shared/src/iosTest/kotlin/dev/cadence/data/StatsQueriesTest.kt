package dev.cadence.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Verifies the Stats aggregate queries: per-exercise volume-over-time and the selector list. */
class StatsQueriesTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun teardown() = database.close()

    @Test
    fun volumeOverTime_sums_sets_per_session_for_the_exercise() = runTest {
        val repo = SessionRepositoryImpl(database, benchPressAssetReader)
        repo.ensureSeeded() // exercisesWithHistory INNER JOINs the catalog, so seed it (as the app does)
        val session = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(session.id, "bench-press")
        val itemId = database.loggedItemDao().getBySession(session.id).first().id
        repo.addSet(session.id, itemId, reps = 10, loadKg = 60.0) // 600
        repo.addSet(session.id, itemId, reps = 5, loadKg = 100.0)  // 500

        val points = repo.observeVolumeOverTime("bench-press").first()
        assertEquals(1, points.size, "one session → one point")
        assertEquals(1100.0, points.first().volume, "volume = 10×60 + 5×100")

        val withHistory = repo.observeExercisesWithHistory().first()
        assertTrue(withHistory.any { it.id == "bench-press" }, "logged exercise appears in selector")
    }
}
