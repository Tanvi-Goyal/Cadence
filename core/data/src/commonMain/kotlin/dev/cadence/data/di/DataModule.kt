package dev.cadence.data.di

import dev.cadence.data.ActiveWorkoutControllerImpl
import dev.cadence.data.AthleteProfileRepositoryImpl
import dev.cadence.data.EntitlementRepositoryImpl
import dev.cadence.data.MuscleImageProviderImpl
import dev.cadence.data.PersonalRecordRepositoryImpl
import dev.cadence.data.PreferencesRepositoryImpl
import dev.cadence.data.RaceGoalRepositoryImpl
import dev.cadence.data.SessionRepositoryImpl
import dev.cadence.domain.ActiveWorkoutController
import dev.cadence.domain.repository.AthleteProfileRepository
import dev.cadence.domain.repository.EntitlementRepository
import dev.cadence.domain.MuscleImageProvider
import dev.cadence.domain.repository.PersonalRecordRepository
import dev.cadence.domain.repository.PreferencesRepository
import dev.cadence.domain.repository.RaceGoalRepository
import dev.cadence.domain.repository.SessionRepository
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Data graph: repository impls bound to their [dev.cadence.domain] interfaces (so consumers depend
 * on the interface, never the impl), plus the wger muscle-diagram provider. Plain constructor
 * wiring — no class annotations.
 */
val dataModule = module {
    single { SessionRepositoryImpl(get(), get(), get(), get()) } bind SessionRepository::class
    // App-scoped live-workout timer: a single instance shared by every surface (owns its own scope).
    single { ActiveWorkoutControllerImpl(get(), get()) } bind ActiveWorkoutController::class
    single { PreferencesRepositoryImpl(get()) } bind PreferencesRepository::class
    single { AthleteProfileRepositoryImpl(get()) } bind AthleteProfileRepository::class
    single { RaceGoalRepositoryImpl(get(), get(), get()) } bind RaceGoalRepository::class
    single { EntitlementRepositoryImpl(get()) } bind EntitlementRepository::class
    single { PersonalRecordRepositoryImpl(get()) } bind PersonalRecordRepository::class
    single { MuscleImageProviderImpl(get()) } bind MuscleImageProvider::class
}
