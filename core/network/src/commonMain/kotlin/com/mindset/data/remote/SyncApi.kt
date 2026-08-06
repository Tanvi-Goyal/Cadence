package com.mindset.data.remote

import com.mindset.contracts.PullResponse
import com.mindset.contracts.PushResponse
import com.mindset.contracts.SessionDto

/**
 * The sync transport. An interface (not the Ktor impl directly) so the [SyncEngine] can be tested
 * against a fake without a live server, and so the transport is swappable.
 */
interface SyncApi {
    suspend fun push(changes: List<SessionDto>): PushResponse
    suspend fun pull(cursor: Long?): PullResponse
}
