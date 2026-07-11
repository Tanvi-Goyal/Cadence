package dev.cadence.di

import dev.cadence.data.local.iosDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS: supply the Room builder from a Documents-directory file path — no Context needed. */
actual val platformModule: Module = module {
    single { iosDatabaseBuilder() }
}
