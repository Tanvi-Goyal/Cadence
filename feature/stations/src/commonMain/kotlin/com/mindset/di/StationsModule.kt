package com.mindset.di

import com.mindset.presentation.StationsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val stationsModule =
    module {
        viewModelOf(::StationsViewModel)
    }
