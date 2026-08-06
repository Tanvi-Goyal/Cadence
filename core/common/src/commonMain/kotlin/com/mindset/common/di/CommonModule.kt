package com.mindset.common.di

import com.mindset.common.UuidGenerator
import com.mindset.common.UuidV7Generator
import org.koin.dsl.module

@OptIn(kotlin.time.ExperimentalTime::class)
val commonModule = module {
    single<kotlin.time.Clock> { kotlin.time.Clock.System }
    single<UuidGenerator> { UuidV7Generator(get()) }
}
