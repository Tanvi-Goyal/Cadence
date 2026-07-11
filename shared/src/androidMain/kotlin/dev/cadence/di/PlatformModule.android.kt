package dev.cadence.di

import dev.cadence.data.local.androidDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android: supply the Room builder using the `Context` Koin holds via `androidContext(...)`. */
actual val platformModule: Module = module {
    single { androidDatabaseBuilder(androidContext()) }
}
