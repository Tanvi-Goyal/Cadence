package dev.cadence.common.di

import dev.cadence.common.UuidGenerator
import dev.cadence.common.UuidV7Generator
import org.koin.dsl.module

/**
 * Identity + time seams. Injectable so repositories and use-cases are deterministic under test
 * (a fixed [kotlin.time.Clock] yields reproducible UUIDv7 prefixes and timestamps).
 */
@OptIn(kotlin.time.ExperimentalTime::class)
val commonModule = module {
    single<kotlin.time.Clock> { kotlin.time.Clock.System }
    single<UuidGenerator> { UuidV7Generator(get()) }
}
