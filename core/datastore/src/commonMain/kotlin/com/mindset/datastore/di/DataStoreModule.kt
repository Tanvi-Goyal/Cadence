package com.mindset.datastore.di

import org.koin.core.module.Module

/**
 * Platform-supplied DataStore binding. Each platform resolves the Preferences DataStore file path
 * differently (Android needs a `Context`, iOS reads the Documents directory), so this is the DI
 * expression of the DataStore seam — the direct analogue of `databasePlatformModule`.
 */
expect val dataStorePlatformModule: Module
