package com.mindset.data.local.di

import com.mindset.data.local.AppDatabase
import com.mindset.data.local.buildDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Database graph: the [AppDatabase] and the DAOs other modules inject directly (the sync engine
 * needs session/outbox/syncMeta DAOs; repositories reach the rest via `database.xxxDao()`).
 */
val databaseModule = module {
    single { buildDatabase(get()) }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().outboxDao() }
    single { get<AppDatabase>().syncMetaDao() }
}

/**
 * Platform-supplied DB bindings. Each platform builds the [androidx.room3.RoomDatabase.Builder]
 * differently (Android needs a `Context`, iOS a file path) and reads seeded assets its own way,
 * so this is the DI expression of the database seam.
 */
expect val databasePlatformModule: Module
