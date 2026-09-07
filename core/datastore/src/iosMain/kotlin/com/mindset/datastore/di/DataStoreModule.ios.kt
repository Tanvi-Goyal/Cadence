package com.mindset.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mindset.datastore.iosPreferencesDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS DataStore seam: the Preferences DataStore (file path from the Documents directory). */
actual val dataStorePlatformModule: Module =
    module {
        single<DataStore<Preferences>> { iosPreferencesDataStore() }
    }
