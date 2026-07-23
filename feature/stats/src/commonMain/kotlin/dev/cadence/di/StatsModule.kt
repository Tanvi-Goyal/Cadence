package dev.cadence.di

import dev.cadence.presentation.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the stats feature. */
val statsModule = module {
    viewModelOf(::StatsViewModel)
}
