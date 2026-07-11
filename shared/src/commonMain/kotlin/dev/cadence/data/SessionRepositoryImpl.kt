package dev.cadence.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.Session
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Room-backed [SessionRepository]. Plain constructor injection ([AppDatabase]) — no DI-framework
 * types leak in, so a later swap of Koin for another framework is a wiring-only change.
 */
class SessionRepositoryImpl(
    private val database: AppDatabase,
) : SessionRepository {

    override fun observeSessions(): Flow<List<Session>> =
        database.sessionDao().observeAll()

    /**
     * The heart of offline-first: the local write and the outbox enqueue happen in ONE
     * transaction via `useWriterConnection { immediateTransaction { … } }` (the KMP-common
     * equivalent of Android's `withTransaction`). If the process dies mid-way, SQLite rolls both
     * back together — there is no window where a session exists locally but its "tell the server"
     * intent was lost, nor vice versa. That atomicity is what makes the outbox trustworthy.
     */
    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun createSession(): Session {
        val now = Clock.System.now().toEpochMilliseconds()
        val session = Session(
            id = Uuid.random().toString(),
            startedAt = now,
            updatedAt = now,
        )
        val outboxEntry = OutboxEntry(
            id = Uuid.random().toString(),
            entityType = "session",
            entityId = session.id,
            opType = "CREATE",
            payload = "",
            createdAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                database.sessionDao().insert(session)
                database.outboxDao().insert(outboxEntry)
            }
        }
        return session
    }
}
