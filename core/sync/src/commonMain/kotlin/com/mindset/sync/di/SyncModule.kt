package com.mindset.sync.di

import com.mindset.domain.Syncer
import com.mindset.sync.SyncEngine
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Sync graph: the [SyncEngine]. Its collaborators (AppDatabase, session/outbox/syncMeta DAOs,
 * [com.mindset.data.remote.SyncApi]) are resolved from the database + network modules in the
 * aggregated graph.
 */
val syncModule =
    module {
        single { SyncEngine(get(), get(), get(), get(), get()) } bind Syncer::class
    }
