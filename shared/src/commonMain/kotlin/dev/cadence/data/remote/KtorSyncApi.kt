package dev.cadence.data.remote

import dev.cadence.contracts.PullRequest
import dev.cadence.contracts.PullResponse
import dev.cadence.contracts.PushRequest
import dev.cadence.contracts.PushResponse
import dev.cadence.contracts.SessionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Ktor-backed [SyncApi]. [baseUrl] is platform-supplied (emulator host differs per platform). */
class KtorSyncApi(
    private val client: HttpClient,
    private val baseUrl: String,
) : SyncApi {

    override suspend fun push(changes: List<SessionDto>): PushResponse =
        client.post("$baseUrl/sync/push") {
            contentType(ContentType.Application.Json)
            setBody(PushRequest(changes))
        }.body()

    override suspend fun pull(cursor: Long?): PullResponse =
        client.post("$baseUrl/sync/pull") {
            contentType(ContentType.Application.Json)
            setBody(PullRequest(cursor))
        }.body()
}
