package com.mindset.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Android's half of the DataStore seam: the file path is resolved under the app's `filesDir`, which
 * is why this needs a [Context] and can't live in commonMain (mirrors `androidDatabaseBuilder`).
 */
fun androidPreferencesDataStore(context: Context): DataStore<Preferences> = createPreferencesDataStore(
    context.applicationContext.filesDir
        .resolve(
            "datastore/$PREFERENCES_DATA_STORE_FILE",
        ).absolutePath,
)
