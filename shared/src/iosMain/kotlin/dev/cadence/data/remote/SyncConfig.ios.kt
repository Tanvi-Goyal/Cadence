package dev.cadence.data.remote

/** The iOS simulator shares the host's network, so localhost is reachable directly. */
actual val syncBaseUrl: String = "http://127.0.0.1:8080"
