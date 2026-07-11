package dev.cadence.data

import dev.cadence.data.local.Session
import kotlinx.coroutines.flow.Flow

/**
 * The app's entry point to session data. Deliberately framework-agnostic (plain interface, plain
 * types) — nothing here knows about Koin, so the DI framework can be swapped without touching it.
 */
interface SessionRepository {

    /** The single source of truth for the UI: a reactive stream of non-deleted sessions. */
    fun observeSessions(): Flow<List<Session>>

    /** Creates a blank session, persisting it and enqueuing its sync mutation atomically. */
    suspend fun createSession(): Session
}
