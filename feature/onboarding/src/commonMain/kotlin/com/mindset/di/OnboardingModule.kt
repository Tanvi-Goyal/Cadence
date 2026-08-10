package com.mindset.di

import com.mindset.presentation.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Onboarding ViewModel graph. Aggregated in `:shared` `Modules.kt` (same package, no import). */
val onboardingModule =
    module {
        viewModelOf(::OnboardingViewModel)
    }
