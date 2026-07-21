package dev.cadence.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.cadence.common.UuidV7Generator
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.SessionType
import dev.cadence.domain.epleyOneRepMax
import dev.cadence.model.CaptureFields
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration coverage for A5: PB detection actually persists (and updates in place) on set writes,
 * and A4b's hydration — `observeSessionDetail` returns the real block graph with derived capture
 * fields. In-memory Room lives in iosTest (Context-free builder), like the other repository tests.
 */
class PbAndHydrationTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun teardown() = database.close()

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun repo() = SessionRepositoryImpl(
        database, benchPressAssetReader, UuidV7Generator(kotlin.time.Clock.System), kotlin.time.Clock.System,
    )

    @Test
    fun completing_sets_records_and_updates_prs_in_place() = runTest {
        val repo = repo()
        repo.ensureSeeded() // seeds the bench-press catalog row (WEIGHT_REPS)
        val session = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(session.id, "bench-press")
        val entryId = database.exerciseEntryDao().getBySession(session.id).first().id

        repo.addSet(session.id, entryId, reps = 5, loadKg = 100.0)

        val prs = database.personalRecordDao().getForExercise("bench-press")
        assertEquals(setOf("EST_1RM", "MAX_WEIGHT"), prs.map { it.kind }.toSet())
        assertEquals(epleyOneRepMax(100.0, 5), prs.first { it.kind == "EST_1RM" }.value)
        assertEquals(100.0, prs.first { it.kind == "MAX_WEIGHT" }.value)

        // A heavier top set updates the SAME rows rather than duplicating them.
        repo.addSet(session.id, entryId, reps = 3, loadKg = 110.0)
        val updated = database.personalRecordDao().getForExercise("bench-press")
        assertEquals(2, updated.size, "records update in place, no duplicates")
        assertEquals(110.0, updated.first { it.kind == "MAX_WEIGHT" }.value)
    }

    @Test
    fun hydrated_session_detail_exposes_one_block_with_capture_fields() = runTest {
        val repo = repo()
        repo.ensureSeeded()
        val session = repo.createSession(SessionType.STRENGTH)
        repo.addExercise(session.id, "bench-press")

        val detail = repo.observeSessionDetail(session.id).first()
        assertNotNull(detail)
        assertEquals(1, detail.blocks.size, "one implicit STRAIGHT block")
        val entry = detail.blocks.first().entries.first()
        assertEquals("bench-press", entry.entry.exerciseId)
        assertEquals(CaptureFields.WeightReps, entry.captureFields, "WEIGHT_REPS ⇒ weight+reps cells")
    }
}
