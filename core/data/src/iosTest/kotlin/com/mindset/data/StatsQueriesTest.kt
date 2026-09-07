package com.mindset.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mindset.common.UuidV7Generator
import com.mindset.data.local.AppDatabase
import com.mindset.data.local.ExerciseAssetReader
import com.mindset.data.local.SessionType
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

    /** Repository wired with the real UUIDv7 + system-clock seam (mirrors production DI). */
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun repoWith(reader: ExerciseAssetReader) =
        SessionRepositoryImpl(database, reader, UuidV7Generator(kotlin.time.Clock.System), kotlin.time.Clock.System)

    @Test
    fun volumeOverTime_sums_sets_per_session_for_the_exercise() = runTest {
        val repo = repoWith(benchPressAssetReader)
        repo.ensureSeeded() // exercisesWithHistory INNER JOINs the catalog, so seed it (as the app does)
        val session = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(session.id, "bench-press")
        val itemId = database.exerciseEntryDao().getBySession(session.id).first().id
        repo.addSet(session.id, itemId, reps = 10, loadKg = 60.0) // 600
        repo.addSet(session.id, itemId, reps = 5, loadKg = 100.0) // 500

        val points = repo.observeVolumeOverTime("bench-press").first()
        assertEquals(1, points.size, "one session → one point")
        assertEquals(1100.0, points.first().volume, "volume = 10×60 + 5×100")

        val withHistory = repo.observeExercisesWithHistory().first()
        assertTrue(withHistory.any { it.id == "bench-press" }, "logged exercise appears in selector")
    }
}
