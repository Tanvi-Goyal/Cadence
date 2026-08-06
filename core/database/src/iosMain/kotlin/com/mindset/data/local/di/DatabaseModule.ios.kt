package com.mindset.data.local.di

import com.mindset.data.local.ExerciseAssetReader
import com.mindset.data.local.iosDatabaseBuilder
import com.mindset.data.local.iosExerciseAssetReader
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS DB seam: the Room builder (file path, no Context) + exercise-catalog reader. */
actual val databasePlatformModule: Module = module {
    single { iosDatabaseBuilder() }
    single<ExerciseAssetReader> { iosExerciseAssetReader() }
}
