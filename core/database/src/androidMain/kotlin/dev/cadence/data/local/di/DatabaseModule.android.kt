package dev.cadence.data.local.di

import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.androidDatabaseBuilder
import dev.cadence.data.local.androidExerciseAssetReader
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android DB seam: the Room builder + exercise-catalog reader, both need a `Context`. */
actual val databasePlatformModule: Module = module {
    single { androidDatabaseBuilder(androidContext()) }
    single<ExerciseAssetReader> { androidExerciseAssetReader(androidContext()) }
}
