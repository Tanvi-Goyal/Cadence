package com.mindset.data.remote

import com.mindset.contracts.PullRequest
import com.mindset.contracts.PullResponse
import com.mindset.contracts.PushRequest
import com.mindset.contracts.PushResponse
import com.mindset.contracts.SessionDto
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
