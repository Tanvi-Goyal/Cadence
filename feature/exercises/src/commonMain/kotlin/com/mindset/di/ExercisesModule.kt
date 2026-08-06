package com.mindset.di

import com.mindset.presentation.ExerciseDetailViewModel
import com.mindset.presentation.ExerciseLibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the exercises feature: the library VM + the parameterized detail VM. */
val exercisesModule = module {
    viewModelOf(::ExerciseLibraryViewModel)
    viewModel { params -> ExerciseDetailViewModel(get(), get(), params.get()) }
}
