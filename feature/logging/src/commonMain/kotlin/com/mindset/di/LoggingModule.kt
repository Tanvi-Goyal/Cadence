package com.mindset.di

import com.mindset.presentation.LogTabViewModel
import com.mindset.presentation.LogWorkoutViewModel
import com.mindset.presentation.NewSessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the logging feature: new-session VM, the Log tab VM, + the parameterized log-workout VM. */
val loggingModule =
    module {
        viewModelOf(::NewSessionViewModel)
        viewModelOf(::LogTabViewModel)
        viewModel { params ->
            LogWorkoutViewModel(
                get(),
                get(),
                get(),
                params.get(),
            )
        }
    }
