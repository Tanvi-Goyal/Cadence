package com.mindset.data.remote

/**
 * Base URL of the sync server. Platform-specific because the loopback host differs: the Android
 * emulator reaches the developer machine at 10.0.2.2, the iOS simulator at 127.0.0.1. Not an
 * OS-governed capability, but the emulator networking difference forces the seam for dev.
 */
expect val syncBaseUrl: String
