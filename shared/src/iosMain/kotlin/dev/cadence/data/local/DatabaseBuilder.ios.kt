package dev.cadence.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS's half of the DB-builder seam: no `Context` exists on iOS, so Room takes just a file path.
 * The path comes from the app's Documents directory via `NSFileManager` — a platform API that
 * has no meaning in commonMain, which is the whole reason this is an `iosMain` source file.
 */
fun iosDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/cadence.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
