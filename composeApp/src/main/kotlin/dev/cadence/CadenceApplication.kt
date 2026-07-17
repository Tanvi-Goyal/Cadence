package dev.cadence

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import dev.cadence.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Starts Koin once per process. The shared [initKoin] wires the common graph; here we hand it the
 * Android-only piece it can't get itself — the application [android.content.Context], which the
 * Room builder needs. This is where the platform half of the DI seam is supplied.
 */
class CadenceApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@CadenceApplication)
        }
    }

    /** Add the SVG decoder so Coil can render wger's SVG muscle diagrams (JPGs still use the default). */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
