package dev.cadence

import android.app.Application
import dev.cadence.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Starts Koin once per process. The shared [initKoin] wires the common graph; here we hand it the
 * Android-only piece it can't get itself — the application [android.content.Context], which the
 * Room builder needs. This is where the platform half of the DI seam is supplied.
 */
class CadenceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@CadenceApplication)
        }
    }
}
