package dev.cadence.data.remote.di

import dev.cadence.data.remote.KtorSyncApi
import dev.cadence.data.remote.SyncApi
import dev.cadence.data.remote.WgerApi
import dev.cadence.data.remote.createHttpClient
import dev.cadence.data.remote.syncBaseUrl
import org.koin.core.module.Module
import org.koin.dsl.module

/** Network graph: HTTP client (from the platform engine) → sync transport + wger client. */
val networkModule = module {
    single { createHttpClient(get()) }
    single<SyncApi> { KtorSyncApi(get(), syncBaseUrl) }
    single { WgerApi(get()) }
}

/**
 * Platform-supplied HTTP engine: OkHttp on Android, Darwin on iOS. The client config is common
 * (see [createHttpClient]); only the engine is platform-specific, so the seam is one type wide.
 */
expect val networkPlatformModule: Module
