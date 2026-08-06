package com.mindset.di

import com.mindset.presentation.LogWorkoutViewModel
import com.mindset.presentation.NewSessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the logging feature: new-session VM + the parameterized log-workout VM. */
val loggingModule = module {
    viewModelOf(::NewSessionViewModel)
    viewModel { params -> LogWorkoutViewModel(get(), get(), params.get()) }
}
