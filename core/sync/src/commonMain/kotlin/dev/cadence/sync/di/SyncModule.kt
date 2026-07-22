package dev.cadence.sync.di

import dev.cadence.sync.SyncEngine
import org.koin.dsl.module

/**
 * Sync graph: the [SyncEngine]. Its collaborators (AppDatabase, session/outbox/syncMeta DAOs,
 * [dev.cadence.data.remote.SyncApi]) are resolved from the database + network modules in the
 * aggregated graph.
 */
val syncModule = module {
    single { SyncEngine(get(), get(), get(), get(), get()) }
}
