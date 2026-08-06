package com.mindset.contracts

import kotlinx.serialization.Serializable

/** Push: the client sends its pending local changes (the drained outbox) to the server. */
@Serializable
data class PushRequest(val changes: List<SessionDto>)

@Serializable
data class PushResponse(val accepted: Int)

/**
 * Pull: the client asks for everything changed since its [cursor]. The cursor is a
 * server-authoritative watermark (a monotonic sequence), NOT a timestamp — this sidesteps
 * client clock skew for "what's new", while `updatedAt` inside each [SessionDto] is still used
 * separately for Last-Write-Wins conflict resolution.
 */
@Serializable
data class PullRequest(val cursor: Long? = null)

@Serializable
data class PullResponse(
    val changes: List<SessionDto>,
    val nextCursor: Long,
)
