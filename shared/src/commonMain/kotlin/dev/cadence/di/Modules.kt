package dev.cadence.di

import dev.cadence.common.di.commonModule
import dev.cadence.data.di.dataModule
import dev.cadence.data.local.di.databaseModule
import dev.cadence.data.local.di.databasePlatformModule
import dev.cadence.data.remote.di.networkModule
import dev.cadence.data.remote.di.networkPlatformModule
import dev.cadence.sync.di.syncModule
import io.kotzilla.generated.monitoring
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Single entry point for starting Koin, called from each platform. It aggregates every module's Koin
 * module: the `:core:*` object graphs + platform seams (B7) and the per-feature ViewModel graphs
 * (B11). Each feature module owns its own `xModule` in package `dev.cadence.di`, so they resolve here
 * with no import. [config] lets a platform add bindings it alone can supply — e.g. Android passes
 * `androidContext(this)`; iOS passes nothing.
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
            // Feature ViewModel graphs (each in package dev.cadence.di in its feature module):
            homeModule,
            loggingModule,
            templatesModule,
            exercisesModule,
            historyModule,
            statsModule,
            profileModule,
            onboardingModule,
        )
        monitoring()
    }
