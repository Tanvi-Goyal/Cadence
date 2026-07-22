package dev.cadence.server

import dev.cadence.contracts.PullRequest
import dev.cadence.contracts.PullResponse
import dev.cadence.contracts.PushRequest
import dev.cadence.contracts.PushResponse
import dev.cadence.contracts.SessionDto
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncRoutesTest {

    private fun session(id: String, updatedAt: Long, deletedAt: Long? = null) =
        SessionDto(id = id, startedAt = 1L, notes = null, updatedAt = updatedAt, deletedAt = deletedAt)

    @Test
    fun push_then_pull_roundtrips_and_advances_cursor() = testApplication {
        application { syncModule() }
        val client = createJsonClient()

        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            setBody(PushRequest(listOf(session("a", updatedAt = 10))))
        }

        val pull: PullResponse = client.post("/sync/pull") {
            contentType(ContentType.Application.Json)
            setBody(PullRequest(cursor = null))
        }.body()

        assertEquals(1, pull.changes.size)
        assertEquals("a", pull.changes.first().id)
        assertTrue(pull.nextCursor > 0, "cursor should advance past the initial 0")

        // Pulling again with the returned cursor yields nothing new.
        val empty: PullResponse = client.post("/sync/pull") {
            contentType(ContentType.Application.Json)
            setBody(PullRequest(cursor = pull.nextCursor))
        }.body()
        assertEquals(0, empty.changes.size)
    }

    @Test
    fun lww_keeps_the_later_updatedAt_and_rejects_the_stale_write() = testApplication {
        application { syncModule() }
        val client = createJsonClient()

        // Newer version lands first, then a stale (older updatedAt) version for the same id arrives.
        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            setBody(PushRequest(listOf(session("a", updatedAt = 20))))
        }
        val stale: PushResponse = client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            setBody(PushRequest(listOf(session("a", updatedAt = 5, deletedAt = 5))))
        }.body()

        assertEquals(0, stale.accepted, "the older write must be rejected by LWW")

        val pull: PullResponse = client.post("/sync/pull") {
            contentType(ContentType.Application.Json)
            setBody(PullRequest(cursor = null))
        }.body()
        assertEquals(1, pull.changes.size)
        assertEquals(20, pull.changes.first().updatedAt, "the newer version must survive")
        assertEquals(null, pull.changes.first().deletedAt)
    }

    @Test
    fun soft_delete_propagates_through_pull() = testApplication {
        application { syncModule() }
        val client = createJsonClient()

        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            setBody(PushRequest(listOf(session("a", updatedAt = 10))))
        }
        client.post("/sync/push") {
            contentType(ContentType.Application.Json)
            setBody(PushRequest(listOf(session("a", updatedAt = 30, deletedAt = 30))))
        }

        val pull: PullResponse = client.post("/sync/pull") {
            contentType(ContentType.Application.Json)
            setBody(PullRequest(cursor = null))
        }.body()
        assertEquals(1, pull.changes.size)
        assertTrue(pull.changes.first().deletedAt != null, "the deletion must be visible to other devices")
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.createJsonClient() =
        createClient { install(ContentNegotiation) { json() } }
}
