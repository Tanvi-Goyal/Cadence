package dev.cadence.data.remote.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android HTTP engine seam. */
actual val networkPlatformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
}
