package dev.cadence.di

import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.androidDatabaseBuilder
import dev.cadence.data.local.androidExerciseAssetReader
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android platform bindings: the Room builder (needs `Context`) and the Ktor OkHttp engine.
 * The base URL (`syncBaseUrl`) is an expect/actual val, not a Koin binding.
 */
actual val platformModule: Module = module {
    single { androidDatabaseBuilder(androidContext()) }
    single<ExerciseAssetReader> { androidExerciseAssetReader(androidContext()) }
    single<HttpClientEngine> { OkHttp.create() }
}
