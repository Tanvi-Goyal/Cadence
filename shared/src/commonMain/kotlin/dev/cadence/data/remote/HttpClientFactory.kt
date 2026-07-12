package dev.cadence.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds the shared [HttpClient] from a platform-supplied [HttpClientEngine] (OkHttp on Android,
 * Darwin on iOS). The client config — JSON content negotiation — is common; only the engine is
 * platform-specific, so the seam is exactly one type wide.
 */
fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
