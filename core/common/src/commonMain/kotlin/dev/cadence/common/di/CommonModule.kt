package dev.cadence.common.di

import dev.cadence.common.UuidGenerator
import dev.cadence.common.UuidV7Generator
import org.koin.dsl.module

@OptIn(kotlin.time.ExperimentalTime::class)
val commonModule = module {
    single<kotlin.time.Clock> { kotlin.time.Clock.System }
    single<UuidGenerator> { UuidV7Generator(get()) }
}
