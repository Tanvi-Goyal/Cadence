package com.mindset.data.local.di

import com.mindset.data.local.ExerciseAssetReader
import com.mindset.data.local.androidDatabaseBuilder
import com.mindset.data.local.androidExerciseAssetReader
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android DB seam: the Room builder + exercise-catalog reader, both need a `Context`. */
actual val databasePlatformModule: Module = module {
    single { androidDatabaseBuilder(androidContext()) }
    single<ExerciseAssetReader> { androidExerciseAssetReader(androidContext()) }
}
