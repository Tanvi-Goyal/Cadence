package dev.cadence.data.local

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSTemporaryDirectory
import platform.posix.getenv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Proves the v7 → v8 fold survives on real legacy data. [MigrationTestHelper] builds the v7 DB from
 * `schemas/…/7.json`, we insert a legacy session tree, then `runMigrationsAndValidate` applies
 * [MIGRATION_7_8] and validates the result against `8.json` column-for-column — so a schema drift in
 * the migration SQL fails here, not in production. Runs on the iOS sim (the Native actual of
 * MigrationTestHelper is driver-based; the Android one is instrumentation-only).
 */
class MigrationTest {

    // The schema dir path is injected by the Gradle test task (see shared/build.gradle.kts) — Room
    // reads the exported JSON from disk at runtime, and the Native test's CWD isn't the module root.
    @OptIn(ExperimentalForeignApi::class)
    private val schemaDir: String = getenv("CADENCE_SCHEMA_DIR")?.toKString()
        ?: error("CADENCE_SCHEMA_DIR not set — check the KotlinNativeSimulatorTest env wiring")

    private val helper = MigrationTestHelper(
        schemaDirectoryPath = schemaDir,
        fileName = NSTemporaryDirectory() + "cadence-migration-test.db",
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class,
    )

    private fun SQLiteConnection.long(sql: String): Long =
        prepare(sql).use { st -> st.step(); st.getLong(0) }

    private fun SQLiteConnection.nullableLong(sql: String): Long? =
        prepare(sql).use { st -> st.step(); if (st.isNull(0)) null else st.getLong(0) }

    @Test
    fun folds_legacy_session_tree_into_blocks_and_backfills_tombstones() = runTest {
        helper.createDatabase(version = 7).apply {
            execSQL(
                "INSERT INTO sessions (id, startedAt, name, type, notes, isTemplate, source, " +
                    "templateId, updatedAt, deleted, syncStatus) VALUES " +
                    "('s1', 1000, 'Upper', 'STRENGTH', NULL, 0, 'MANUAL', NULL, 2000, 0, 'SYNCED')",
            )
            // A soft-deleted, empty session — its tombstone must carry over, and it gets no block.
            execSQL(
                "INSERT INTO sessions (id, startedAt, name, type, notes, isTemplate, source, " +
                    "templateId, updatedAt, deleted, syncStatus) VALUES " +
                    "('s2', 500, 'Old', 'STRENGTH', NULL, 0, 'MANUAL', NULL, 900, 1, 'SYNCED')",
            )
            execSQL(
                "INSERT INTO exercises (id, name, category, metric, primaryMuscles, secondaryMuscles, " +
                    "instructions, imageUrls, keywords) VALUES " +
                    "('bench-press', 'Bench', 'strength', 'WEIGHT_REPS', '', '', '', '', '')",
            )
            execSQL("INSERT INTO logged_items (id, sessionId, exerciseId, orderIndex) VALUES ('li1', 's1', 'bench-press', 0)")
            execSQL("INSERT INTO set_entries (id, loggedItemId, setNumber, reps, loadKg) VALUES ('se1', 'li1', 1, 10, 60.0)")
            execSQL("INSERT INTO set_entries (id, loggedItemId, setNumber, reps, loadKg) VALUES ('se2', 'li1', 2, 8, 65.0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(version = 8, migrations = listOf(MIGRATION_7_8))

        // Fold: s1 (with entries) gets exactly one implicit STRAIGHT block; s2 (empty) gets none.
        assertEquals(1L, db.long("SELECT COUNT(*) FROM blocks"))
        assertEquals(1L, db.long("SELECT COUNT(*) FROM blocks WHERE id = 'block-s1' AND type = 'STRAIGHT' AND sessionId = 's1'"))
        assertEquals(1L, db.long("SELECT COUNT(*) FROM exercise_entries WHERE id = 'li1' AND blockId = 'block-s1' AND exerciseId = 'bench-press'"))
        assertEquals(2L, db.long("SELECT COUNT(*) FROM set_entries WHERE exerciseEntryId = 'li1'"))

        // Tombstone cutover: s1 stays live; s2's deletedAt is backfilled from its updatedAt.
        assertEquals(null, db.nullableLong("SELECT deletedAt FROM sessions WHERE id = 's1'"))
        assertEquals(900L, db.nullableLong("SELECT deletedAt FROM sessions WHERE id = 's2'"))
        // createdAt backfilled from startedAt; ids preserved.
        assertEquals(1000L, db.long("SELECT createdAt FROM sessions WHERE id = 's1'"))

        // Exercises kept; the new modality column exists and is null (A7 populates it).
        assertEquals(1L, db.long("SELECT COUNT(*) FROM exercises WHERE id = 'bench-press'"))
        assertTrue(db.long("SELECT modality IS NULL FROM exercises WHERE id = 'bench-press'") == 1L)

        db.close()
    }
}
