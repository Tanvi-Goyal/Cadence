package com.mindset.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mindset.datastore.androidPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android DataStore seam: the Preferences DataStore, whose path needs a `Context`. */
actual val dataStorePlatformModule: Module =
    module {
        single<DataStore<Preferences>> { androidPreferencesDataStore(androidContext()) }
    }
