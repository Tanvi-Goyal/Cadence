package dev.cadence.data.local

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * Finalizes a platform-supplied [RoomDatabase.Builder] into an [AppDatabase].
 *
 * The driver is [BundledSQLiteDriver] — Room-KMP ships one bundled SQLite for BOTH Android and
 * iOS, so this line is plain common code, not an expect/actual. The platform seam is only the
 * *builder* (see `androidDatabaseBuilder` / `iosDatabaseBuilder`): Android needs a `Context` to
 * resolve a db path, iOS just needs a file path string. Query coroutine context is left unset —
 * Room defaults it to `Dispatchers.IO`, which isn't referenceable from commonMain anyway.
 */
fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        // Real versioned migration (no more destructive fallback): existing installs upgrade v7 → v8
        // without data loss. See [MIGRATION_7_8]. Future schema changes add the next migration here.
        .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
        .build()
