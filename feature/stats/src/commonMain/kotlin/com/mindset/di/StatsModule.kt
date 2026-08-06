package com.mindset.di

import com.mindset.presentation.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the stats feature. */
val statsModule = module {
    viewModelOf(::StatsViewModel)
}
