package dev.cadence.data.remote

import dev.cadence.contracts.PullResponse
import dev.cadence.contracts.PushResponse
import dev.cadence.contracts.SessionDto

/**
 * The sync transport. An interface (not the Ktor impl directly) so the [SyncEngine] can be tested
 * against a fake without a live server, and so the transport is swappable.
 */
interface SyncApi {
    suspend fun push(changes: List<SessionDto>): PushResponse
    suspend fun pull(cursor: Long?): PullResponse
}
