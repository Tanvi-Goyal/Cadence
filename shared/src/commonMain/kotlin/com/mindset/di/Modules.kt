package com.mindset.di

import com.mindset.common.di.commonModule
import com.mindset.data.di.dataModule
import com.mindset.data.local.di.databaseModule
import com.mindset.data.local.di.databasePlatformModule
import com.mindset.data.remote.di.networkModule
import com.mindset.data.remote.di.networkPlatformModule
import com.mindset.datastore.di.dataStorePlatformModule
import com.mindset.sync.di.syncModule
import io.kotzilla.generated.monitoring
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Single entry point for starting Koin, called from each platform. It aggregates every module's Koin
 * module: the `:core:*` object graphs + platform seams (B7) and the per-feature ViewModel graphs
 * (B11). Each feature module owns its own `xModule` in package `com.mindset.di`, so they resolve here
 * with no import. [config] lets a platform add bindings it alone can supply — e.g. Android passes
 * `androidContext(this)`; iOS passes nothing.
 *
 * [observability] is applied AFTER [modules] — the Kotzilla profiler inspects the assembled graph,
 * so the ordering is load-bearing. It is a parameter rather than a call here because a profiler
 * must not ship to users: only a debug build passes [kotzillaMonitoring].
 */
fun initKoin(
    config: KoinAppDeclaration? = null,
    observability: KoinAppDeclaration? = null,
): KoinApplication = startKoin {
    config?.invoke(this)
    modules(
        commonModule,
        databaseModule,
        databasePlatformModule,
        dataStorePlatformModule,
        networkModule,
        networkPlatformModule,
        dataModule,
        syncModule,
        // Feature ViewModel graphs (each in package com.mindset.di in its feature module):
        homeModule,
        loggingModule,
        templatesModule,
        exercisesModule,
        historyModule,
        stationsModule,
        profileModule,
        onboardingModule,
    )
    observability?.invoke(this)
}

/**
 * Kotzilla Koin profiler wiring — streams the DI graph and runtime telemetry to Kotzilla's cloud,
 * and carries the API key generated from `shared/kotzilla.json`. Pass it to [initKoin] ONLY from a
 * debug build; referencing it from a release build ships a profiler, and its data collection, to
 * every user.
 */
val kotzillaMonitoring: KoinAppDeclaration = { monitoring() }
