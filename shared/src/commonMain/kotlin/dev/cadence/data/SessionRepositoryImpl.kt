package dev.cadence.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import dev.cadence.data.local.SessionType
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

    override fun observePlannedSession(): Flow<PlannedSession?> =
        database.plannedSessionDao().observeCurrent()

    /** Blank/ad-hoc session with defaults. */
    override suspend fun createSession(): Session =
        insertSession(name = "Session", type = SessionType.STRENGTH)

    /** Start the planned session — the real session carries the plan's name/type. */
    override suspend fun startPlannedSession(plan: PlannedSession): Session =
        insertSession(name = plan.name, type = plan.type)

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private suspend fun insertSession(name: String, type: String): Session {
        val now = Clock.System.now().toEpochMilliseconds()
        val session = Session(
            id = Uuid.random().toString(),
            startedAt = now,
            name = name,
            type = type,
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
        // Local write + outbox enqueue in ONE transaction — the offline-first atomicity guarantee.
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                database.sessionDao().insert(session)
                database.outboxDao().insert(outboxEntry)
            }
        }
        return session
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun ensureSeeded() {
        if (database.plannedSessionDao().getCurrent() != null) return
        database.plannedSessionDao().upsert(
            PlannedSession(
                id = Uuid.random().toString(),
                name = "Upper Strength",
                type = SessionType.STRENGTH,
                targetDurationMin = 55,
                focus = "Push focus",
            ),
        )
    }
}
