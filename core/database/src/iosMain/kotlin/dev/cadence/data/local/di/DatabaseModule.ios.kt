package dev.cadence.data.local.di

import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.iosDatabaseBuilder
import dev.cadence.data.local.iosExerciseAssetReader
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS DB seam: the Room builder (file path, no Context) + exercise-catalog reader. */
actual val databasePlatformModule: Module = module {
    single { iosDatabaseBuilder() }
    single<ExerciseAssetReader> { iosExerciseAssetReader() }
}
