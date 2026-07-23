package dev.cadence.di

import dev.cadence.presentation.LogWorkoutViewModel
import dev.cadence.presentation.NewSessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the logging feature: new-session VM + the parameterized log-workout VM. */
val loggingModule = module {
    viewModelOf(::NewSessionViewModel)
    viewModel { params -> LogWorkoutViewModel(get(), params.get()) }
}
