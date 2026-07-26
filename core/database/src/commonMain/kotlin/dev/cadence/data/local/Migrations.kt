package dev.cadence.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * v7 → v8. The pre-rethink 3-level tree (session → logged_item → set) becomes the 4-level model
 * (session → block → exercise_entry → set); sync tombstones (`deletedAt`) replace the boolean
 * `deleted`; the PB cache and modality columns arrive.
 *
 * **Why table-recreate, not `ALTER TABLE ADD COLUMN`, for `sessions`/`set_entries`:** SQLite can
 * only add a NOT NULL column *with a DEFAULT*, but Room's generated schema declares no default, so
 * Room's post-migration schema validation would reject the mismatch. Recreating each changed table
 * from Room's exact `createSql` (copied verbatim from `schemas/8.json`) sidesteps that — the result
 * validates column-for-column. `exercises` only gains NULLABLE columns, so plain ADD COLUMN is fine.
 *
 * **Legacy fold:** each `logged_item` becomes an `exercise_entry` under one implicit STRAIGHT block
 * per session (id `block-<sessionId>`), matching the runtime convention in `Mappers`/repo. Child
 * envelope columns backfill from the parent session (`createdAt = startedAt`, tombstone from the old
 * `deleted`). Client-generated PKs are **preserved, never regenerated** — they are the sync identity.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // 1. sessions → recreate: + createdAt (backfilled from startedAt), + deletedAt (from `deleted`),
        //    − deleted.
        connection.execSQL(
            "CREATE TABLE `sessions_new` (`id` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, `type` TEXT NOT NULL, `notes` TEXT, `isTemplate` INTEGER NOT NULL, " +
                "`source` TEXT NOT NULL, `templateId` TEXT, `updatedAt` INTEGER NOT NULL, " +
                "`syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `deletedAt` INTEGER, " +
                "PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "INSERT INTO `sessions_new` (id, startedAt, name, type, notes, isTemplate, source, " +
                "templateId, updatedAt, syncStatus, createdAt, deletedAt) " +
                "SELECT id, startedAt, name, type, notes, isTemplate, source, templateId, updatedAt, " +
                "syncStatus, startedAt, CASE WHEN deleted = 1 THEN updatedAt ELSE NULL END FROM `sessions`",
        )
        connection.execSQL("DROP TABLE `sessions`")
        connection.execSQL("ALTER TABLE `sessions_new` RENAME TO `sessions`")

        // 2. blocks → new; one implicit STRAIGHT block per session that actually has entries.
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `blocks` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, " +
                "`type` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `rounds` INTEGER, " +
                "`restBetweenRoundsMs` INTEGER, `label` TEXT, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, PRIMARY KEY(`id`))",
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_blocks_sessionId` ON `blocks` (`sessionId`)")
        connection.execSQL(
            "INSERT INTO `blocks` (id, sessionId, type, orderIndex, rounds, restBetweenRoundsMs, " +
                "label, createdAt, updatedAt, deletedAt) " +
                "SELECT 'block-' || s.id, s.id, 'STRAIGHT', 0, 1, NULL, NULL, s.createdAt, s.updatedAt, NULL " +
                "FROM `sessions` s WHERE EXISTS (SELECT 1 FROM `logged_items` li WHERE li.sessionId = s.id)",
        )

        // 3. exercise_entries → from logged_items, reparented to the implicit block.
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `exercise_entries` (`id` TEXT NOT NULL, `blockId` TEXT NOT NULL, " +
                "`exerciseId` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `targetSets` INTEGER, " +
                "`restMs` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                "`deletedAt` INTEGER, PRIMARY KEY(`id`))",
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_entries_blockId` ON `exercise_entries` (`blockId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_entries_exerciseId` ON `exercise_entries` (`exerciseId`)")
        connection.execSQL(
            "INSERT INTO `exercise_entries` (id, blockId, exerciseId, orderIndex, targetSets, restMs, " +
                "createdAt, updatedAt, deletedAt) " +
                "SELECT li.id, 'block-' || li.sessionId, li.exerciseId, li.orderIndex, NULL, NULL, " +
                "s.createdAt, s.updatedAt, NULL FROM `logged_items` li JOIN `sessions` s ON s.id = li.sessionId",
        )
        connection.execSQL("DROP TABLE `logged_items`")

        // 4. set_entries → recreate: loggedItemId → exerciseEntryId, + calories/targetCalories + envelope.
        connection.execSQL(
            "CREATE TABLE `set_entries_new` (`id` TEXT NOT NULL, `exerciseEntryId` TEXT NOT NULL, " +
                "`setNumber` INTEGER NOT NULL, `reps` INTEGER, `loadKg` REAL, `timeSec` INTEGER, " +
                "`distanceM` INTEGER, `rpe` INTEGER, `targetReps` INTEGER, `targetLoadKg` REAL, " +
                "`targetTimeSec` INTEGER, `targetDistanceM` INTEGER, `calories` INTEGER, " +
                "`targetCalories` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                "`deletedAt` INTEGER, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "INSERT INTO `set_entries_new` (id, exerciseEntryId, setNumber, reps, loadKg, timeSec, " +
                "distanceM, rpe, targetReps, targetLoadKg, targetTimeSec, targetDistanceM, calories, " +
                "targetCalories, createdAt, updatedAt, deletedAt) " +
                "SELECT id, loggedItemId, setNumber, reps, loadKg, timeSec, distanceM, rpe, targetReps, " +
                "targetLoadKg, targetTimeSec, targetDistanceM, NULL, NULL, 0, 0, NULL FROM `set_entries`",
        )
        connection.execSQL("DROP TABLE `set_entries`")
        connection.execSQL("ALTER TABLE `set_entries_new` RENAME TO `set_entries`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_set_entries_exerciseEntryId` ON `set_entries` (`exerciseEntryId`)")

        // 5. exercises → add nullable modality columns + indices (rows kept; A7 populates values).
        connection.execSQL("ALTER TABLE `exercises` ADD COLUMN `modality` TEXT")
        connection.execSQL("ALTER TABLE `exercises` ADD COLUMN `defaultMetric` TEXT")
        connection.execSQL("ALTER TABLE `exercises` ADD COLUMN `hyroxStation` TEXT")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_modality` ON `exercises` (`modality`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_hyroxStation` ON `exercises` (`hyroxStation`)")

        // 6. personal_records → new, empty (recomputable from history later).
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `personal_records` (`id` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, " +
                "`kind` TEXT NOT NULL, `value` REAL NOT NULL, `distanceBucketM` INTEGER, " +
                "`achievedAt` INTEGER NOT NULL, `sourceSetId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, PRIMARY KEY(`id`))",
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_records_exerciseId` ON `personal_records` (`exerciseId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_records_exerciseId_kind_distanceBucketM` ON `personal_records` (`exerciseId`, `kind`, `distanceBucketM`)")
    }
}

/**
 * v8 → v9. Adds the training-data-model columns needed to represent real programs: conditioning
 * shape (`conditioningFormat`/`capSeconds`/`workSeconds`) and section grouping (`section`) on blocks;
 * a coaching `note` + per-side `eachSide` flag on entries; and lightweight template metadata
 * (`category`/`focus`/`programWeek`) on sessions.
 *
 * **Purely additive, all NULLABLE.** SQLite rejects a NOT-NULL added column without a DEFAULT, and
 * Room's generated schema declares no default (see [MIGRATION_7_8]) — so every new column is nullable
 * and this is plain `ALTER TABLE ADD COLUMN`, no table-recreate, no data touched.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `blocks` ADD COLUMN `section` TEXT")
        connection.execSQL("ALTER TABLE `blocks` ADD COLUMN `conditioningFormat` TEXT")
        connection.execSQL("ALTER TABLE `blocks` ADD COLUMN `capSeconds` INTEGER")
        connection.execSQL("ALTER TABLE `blocks` ADD COLUMN `workSeconds` INTEGER")
        connection.execSQL("ALTER TABLE `exercise_entries` ADD COLUMN `note` TEXT")
        connection.execSQL("ALTER TABLE `exercise_entries` ADD COLUMN `eachSide` INTEGER")
        connection.execSQL("ALTER TABLE `sessions` ADD COLUMN `category` TEXT")
        connection.execSQL("ALTER TABLE `sessions` ADD COLUMN `focus` TEXT")
        connection.execSQL("ALTER TABLE `sessions` ADD COLUMN `programWeek` INTEGER")
    }
}
