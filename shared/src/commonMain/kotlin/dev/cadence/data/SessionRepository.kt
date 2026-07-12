package dev.cadence.data

import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import kotlinx.coroutines.flow.Flow

/**
 * The app's entry point to session data. Deliberately framework-agnostic (plain interface, plain
 * types) — nothing here knows about Koin, so the DI framework can be swapped without touching it.
 */
interface SessionRepository {

    /** The single source of truth for the UI: a reactive stream of non-deleted sessions. */
    fun observeSessions(): Flow<List<Session>>

    /** The current planned/next session shown on the Home "Today" card (null if none). */
    fun observePlannedSession(): Flow<PlannedSession?>

    /** Creates a blank session, persisting it and enqueuing its sync mutation atomically. */
    suspend fun createSession(): Session

    /** Starts a real session from a plan (carries its name/type), atomically with its outbox row. */
    suspend fun startPlannedSession(plan: PlannedSession): Session

    /** Inserts a sensible default plan if none exists, so the Today card has real content. */
    suspend fun ensureSeeded()
}
