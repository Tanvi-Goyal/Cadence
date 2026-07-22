package dev.cadence.data.di

import dev.cadence.data.MuscleImageProvider
import dev.cadence.data.PersonalRecordRepositoryImpl
import dev.cadence.data.PreferencesRepositoryImpl
import dev.cadence.data.SessionRepositoryImpl
import dev.cadence.domain.PersonalRecordRepository
import dev.cadence.domain.PreferencesRepository
import dev.cadence.domain.SessionRepository
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Data graph: repository impls bound to their [dev.cadence.domain] interfaces (so consumers depend
 * on the interface, never the impl), plus the wger muscle-diagram provider. Plain constructor
 * wiring — no class annotations.
 */
val dataModule = module {
    single { SessionRepositoryImpl(get(), get(), get(), get()) } bind SessionRepository::class
    single { PreferencesRepositoryImpl(get()) } bind PreferencesRepository::class
    single { PersonalRecordRepositoryImpl(get()) } bind PersonalRecordRepository::class
    single { MuscleImageProvider(get()) }
}
