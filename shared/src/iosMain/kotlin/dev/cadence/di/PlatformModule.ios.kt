package dev.cadence.di

import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.iosDatabaseBuilder
import dev.cadence.data.local.iosExerciseAssetReader
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS platform bindings: the Room builder (file path, no Context) and the Ktor Darwin engine. */
actual val platformModule: Module = module {
    single { iosDatabaseBuilder() }
    single<ExerciseAssetReader> { iosExerciseAssetReader() }
    single<HttpClientEngine> { Darwin.create() }
}
