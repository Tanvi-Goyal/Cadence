package com.mindset.di

import com.mindset.presentation.HistoryViewModel
import com.mindset.presentation.SessionDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the history feature: the list VM + the parameterized session-detail VM. */
val historyModule = module {
    viewModelOf(::HistoryViewModel)
    viewModel { params -> SessionDetailViewModel(get(), params.get()) }
}
