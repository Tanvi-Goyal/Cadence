package com.mindset.data.remote.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS HTTP engine seam. */
actual val networkPlatformModule: Module =
    module {
        single<HttpClientEngine> { Darwin.create() }
    }
