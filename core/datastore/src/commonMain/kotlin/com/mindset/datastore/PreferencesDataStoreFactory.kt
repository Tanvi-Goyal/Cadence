package com.mindset.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/** File name for the app's single Preferences DataStore (device-local settings). */
const val PREFERENCES_DATA_STORE_FILE = "mindset.preferences_pb"

/**
 * Builds the app's Preferences [DataStore] at [path]. The path is the platform half of the seam —
 * Android resolves it from `Context.filesDir`, iOS from the Documents directory — mirroring the
 * Room database-builder seam. Kept in commonMain because the DataStore construction itself is
 * platform-agnostic; only the file path needs a platform API.
 */
fun createPreferencesDataStore(path: String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })
