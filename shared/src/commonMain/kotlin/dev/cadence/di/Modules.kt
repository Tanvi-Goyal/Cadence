package dev.cadence.di

import dev.cadence.data.MuscleImageProvider
import dev.cadence.data.PreferencesRepository
import dev.cadence.data.PreferencesRepositoryImpl
import dev.cadence.common.UuidGenerator
import dev.cadence.common.UuidV7Generator
import dev.cadence.data.SessionRepository
import dev.cadence.data.SessionRepositoryImpl
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.buildDatabase
import dev.cadence.data.remote.KtorSyncApi
import dev.cadence.data.remote.SyncApi
import dev.cadence.data.remote.WgerApi
import dev.cadence.data.remote.createHttpClient
import dev.cadence.data.remote.syncBaseUrl
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
import dev.cadence.sync.SyncEngine
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Platform-supplied bindings. Each platform provides a [RoomDatabase.Builder] differently
 * (Android needs a `Context`, iOS a file path), so this is the DI expression of the DB seam.
 */
expect val platformModule: Module

/** Shared data graph: DB → DAOs → repository. Plain constructor wiring, no class annotations. */
@OptIn(kotlin.time.ExperimentalTime::class)
val dataModule = module {
    // Identity + time seams (A1): injectable so repositories/use-cases are deterministic under test.
    single<kotlin.time.Clock> { kotlin.time.Clock.System }
    single<UuidGenerator> { UuidV7Generator(get()) }
    single { buildDatabase(get()) }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().outboxDao() }
    single { get<AppDatabase>().syncMetaDao() }
    single { SessionRepositoryImpl(get(), get(), get(), get()) } bind SessionRepository::class
    single { PreferencesRepositoryImpl(get()) } bind PreferencesRepository::class
}

/** Sync graph: HTTP client (from the platform engine) → transport → engine. */
val networkModule = module {
    single { createHttpClient(get()) }
    single<SyncApi> { KtorSyncApi(get(), syncBaseUrl) }
    single { SyncEngine(get(), get(), get(), get(), get()) }
    single { WgerApi(get()) }
    single { MuscleImageProvider(get()) }
}

/** Shared presentation graph. */
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
 * Single entry point for starting Koin, called from each platform. [config] lets a platform add
 * bindings it alone can supply — e.g. Android passes `androidContext(this)`; iOS passes nothing.
 */
fun initKoin(config: KoinAppDeclaration? = null): KoinApplication =
    startKoin {
        config?.invoke(this)
        modules(platformModule, dataModule, networkModule, viewModelModule)
    }
