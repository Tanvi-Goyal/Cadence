package dev.cadence.di

/**
 * Swift-friendly entry point, callable from `iOSApp.init()`. In a clean-named file so the
 * generated Objective-C/Swift facade is the predictable `KoinIosKt`.
 */
fun doInitKoin() {
    initKoin()
}
