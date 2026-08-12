package com.mindset.di

import com.mindset.presentation.StationsViewModel
import com.mindset.presentation.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin graph for the stations feature. [StationsViewModel] backs the Android Station board;
 * [StatsViewModel] is the legacy volume-trend VM still consumed by the iOS `StatsView` (parity
 * port is post-race).
 */
val stationsModule =
    module {
        viewModelOf(::StationsViewModel)
        viewModelOf(::StatsViewModel)
    }
