package com.mindset.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS's half of the DataStore seam: no `Context` exists, so the path comes from the app's Documents
 * directory via `NSFileManager` — the same platform API `iosDatabaseBuilder` uses for the DB file.
 */
fun iosPreferencesDataStore(): DataStore<Preferences> = createPreferencesDataStore("${documentDirectory()}/$PREFERENCES_DATA_STORE_FILE")

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    return requireNotNull(documentDirectory?.path)
}
