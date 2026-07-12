package dev.cadence.data.local

import androidx.room.RoomDatabase
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
        // Dev-only: the SyncMeta table bumped the schema to v2. Destructive migration wipes local
        // data on a schema change rather than shipping a real migration — acceptable pre-release,
        // and safe because the server is the durable copy once sync is on. Replace before any real users.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
