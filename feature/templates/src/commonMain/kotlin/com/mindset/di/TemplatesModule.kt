package com.mindset.di

import com.mindset.presentation.TemplateBuilderViewModel
import com.mindset.presentation.TemplateDetailViewModel
import com.mindset.presentation.TemplateHyroxDetailViewModel
import com.mindset.presentation.TemplateLibraryViewModel
import com.mindset.presentation.TemplatesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the templates feature: the library + detail VMs, the list/create VM + the parameterized builder VM. */
val templatesModule =
    module {
        viewModel { TemplateLibraryViewModel(get()) }
        viewModelOf(::TemplateDetailViewModel)
        viewModel { params -> TemplateHyroxDetailViewModel(get(), get(), get(), params.get()) }
//        viewModel { params -> TemplateStrengthDetailViewModel(get(), params.get()) }
        viewModelOf(::TemplatesViewModel)
        viewModel { params -> TemplateBuilderViewModel(get(), params.get()) }
    }
