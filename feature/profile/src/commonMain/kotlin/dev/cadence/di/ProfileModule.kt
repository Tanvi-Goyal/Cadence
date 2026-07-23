package dev.cadence.di

import dev.cadence.presentation.PreferencesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the profile feature: the preferences ViewModel. Aggregated by `initKoin`. */
val profileModule = module {
    viewModelOf(::PreferencesViewModel)
}
