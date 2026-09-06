package com.mindset

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.mindset.di.initKoin
import com.mindset.di.kotzillaMonitoring
import com.mindset.domain.ActiveWorkoutController
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext

/**
 * Starts Koin once per process. The shared [initKoin] wires the common graph; here we hand it the
 * Android-only piece it can't get itself — the application [android.content.Context], which the
 * Room builder needs. This is where the platform half of the DI seam is supplied, and where the
 * debug-only Koin profiler is gated in.
 */
class MindSetApplication :
    Application(),
    SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            config = {
                androidLogger()
                androidContext(this@MindSetApplication)
            },
            // Debug only. BuildConfig.DEBUG is a compile-time constant, so R8 folds this to null
            // and shrinks the Kotzilla SDK out of every release-derived variant entirely — the
            // profiler and its API key never reach a user's device.
            observability = if (BuildConfig.DEBUG) kotzillaMonitoring else null,
        )
        // Rehydrate a race that outlived the process. Fire-and-forget and idempotent — it does its
        // reads on the controller's own scope, so it never blocks startup, and a restored race is
        // published paused for the athlete to resume.
        GlobalContext.get().get<ActiveWorkoutController>().restore()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader
        .Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .build()
}
