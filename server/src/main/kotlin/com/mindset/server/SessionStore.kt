package com.mindset.server

import com.mindset.contracts.SessionDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Server-side storage for synced sessions. Interface so the in-memory impl can be swapped for a
 * real database later without touching the routes.
 */
interface SessionStore {
    /** Apply an incoming change under Last-Write-Wins. Returns true if it was accepted. */
    suspend fun upsert(dto: SessionDto): Boolean

    /** All rows whose server sequence is strictly greater than [cursor] (null = from the start). */
    suspend fun changesSince(cursor: Long?): List<SessionDto>

    /** The current high-water sequence — what a client should store as its next cursor. */
    suspend fun currentSeq(): Long
}

/**
 * In-memory [SessionStore]. A single anonymous logical user for v1 (one global map); a production
 * server would key everything by userId. Netty serves requests concurrently, so the map and the
 * sequence counter are guarded by a [Mutex].
 *
 * Each stored row carries a monotonic [StoredSession.serverSeq] — the pull watermark. This is
 * deliberately separate from the DTO's `updatedAt`: `serverSeq` answers "what changed since you
 * last pulled?" (immune to client clock skew), while `updatedAt` answers "which of two conflicting
 * versions wins?" (the LWW rule below).
 */
class InMemorySessionStore : SessionStore {
    private data class StoredSession(val dto: SessionDto, val serverSeq: Long)

    private val mutex = Mutex()
    private val rows = mutableMapOf<String, StoredSession>()
    private var seq = 0L

    override suspend fun upsert(dto: SessionDto): Boolean = mutex.withLock {
        val existing = rows[dto.id]
        // Last-Write-Wins: accept only if new, or at least as recent as what we hold.
        if (existing != null && dto.updatedAt < existing.dto.updatedAt) {
            return false
        }
        seq += 1
        rows[dto.id] = StoredSession(dto, seq)
        true
    }

    override suspend fun changesSince(cursor: Long?): List<SessionDto> = mutex.withLock {
        val since = cursor ?: 0L
        rows.values
            .filter { it.serverSeq > since }
            .sortedBy { it.serverSeq }
            .map { it.dto }
    }

    override suspend fun currentSeq(): Long = mutex.withLock { seq }
}
