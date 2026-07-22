package dev.cadence.di

import dev.cadence.common.di.commonModule
import dev.cadence.data.di.dataModule
import dev.cadence.data.local.di.databaseModule
import dev.cadence.data.local.di.databasePlatformModule
import dev.cadence.data.remote.di.networkModule
import dev.cadence.data.remote.di.networkPlatformModule
import dev.cadence.presentation.ExerciseDetailViewModel
import dev.cadence.presentation.ExerciseLibraryViewModel
import dev.cadence.presentation.HistoryViewModel
import dev.cadence.presentation.HomeViewModel
import dev.cadence.presentation.LogWorkoutViewModel
import dev.cadence.presentation.NewSessionViewModel
import dev.cadence.presentation.PreferencesViewModel
import dev.cadence.presentation.SessionDetailViewModel
import dev.cadence.presentation.StatsViewModel
import dev.cadence.presentation.TemplateBuilderViewModel
import dev.cadence.presentation.TemplatesViewModel
import dev.cadence.sync.di.syncModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Shared presentation graph. The data/network/sync object graphs each live in their owning
 * `:core:*` module now (B7); this module still owns the ViewModels until they move to
 * `:feature:*` in B11.
 */
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::ExerciseLibraryViewModel)
    viewModelOf(::NewSessionViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::PreferencesViewModel)
    viewModelOf(::TemplatesViewModel)
    // LogWorkout + SessionDetail + TemplateBuilder need a runtime id → parameterized factories.
    viewModel { params -> LogWorkoutViewModel(get(), params.get()) }
    viewModel { params -> SessionDetailViewModel(get(), params.get()) }
    viewModel { params -> ExerciseDetailViewModel(get(), get(), params.get()) }
    viewModel { params -> TemplateBuilderViewModel(get(), params.get()) }
}

/**
 * Single entry point for starting Koin, called from each platform. It aggregates every module's
 * Koin module (each `:core:*` owns its own graph + platform seam). [config] lets a platform add
 * bindings it alone can supply — e.g. Android passes `androidContext(this)`; iOS passes nothing.
 */
fun initKoin(config: KoinAppDeclaration? = null): KoinApplication =
    startKoin {
        config?.invoke(this)
        modules(
            commonModule,
            databaseModule,
            databasePlatformModule,
            networkModule,
            networkPlatformModule,
            dataModule,
            syncModule,
            viewModelModule,
        )
    }
