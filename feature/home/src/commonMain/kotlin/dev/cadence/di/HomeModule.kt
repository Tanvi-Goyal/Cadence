package dev.cadence.di

import dev.cadence.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the home feature. */
val homeModule = module {
    viewModelOf(::HomeViewModel)
}
