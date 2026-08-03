package dev.cadence.di

import dev.cadence.presentation.TemplateBuilderViewModel
import dev.cadence.presentation.TemplateDetailViewModel
import dev.cadence.presentation.TemplateHyroxDetailViewModel
import dev.cadence.presentation.TemplateLibraryViewModel
import dev.cadence.presentation.TemplateStrengthDetailViewModel
import dev.cadence.presentation.TemplatesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin graph for the templates feature: the library + detail VMs, the list/create VM + the parameterized builder VM. */
val templatesModule = module {
    viewModel { TemplateLibraryViewModel(get()) }
    viewModelOf(::TemplateDetailViewModel)
    viewModel { params -> TemplateHyroxDetailViewModel(get(), params.get()) }
    viewModel { params -> TemplateStrengthDetailViewModel(get(), params.get()) }
    viewModelOf(::TemplatesViewModel)
    viewModel { params -> TemplateBuilderViewModel(get(), params.get()) }
}
