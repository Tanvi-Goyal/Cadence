package com.mindset.di

import com.mindset.presentation.PreferencesViewModel
import com.mindset.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin graph for the profile feature.
 *
 * Two ViewModels on purpose: [ProfileViewModel] backs the Profile *screen*, while
 * [PreferencesViewModel] is the app-wide preferences holder that `MainActivity` and iOS's
 * `PreferencesStore` both resolve. Keeping them separate is what stops a Profile redesign from
 * reshaping the Swift bridge.
 */
val profileModule = module {
    viewModelOf(::PreferencesViewModel)
    viewModelOf(::ProfileViewModel)
}
