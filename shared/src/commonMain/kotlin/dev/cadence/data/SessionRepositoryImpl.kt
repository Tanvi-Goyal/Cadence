package dev.cadence.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.Exercise
import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.ExerciseImporter
import dev.cadence.data.local.LoggedItem
import dev.cadence.data.local.LoggedItemWithSets
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import dev.cadence.data.local.SessionType
import dev.cadence.data.local.SetEntry
import dev.cadence.data.local.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Room-backed [SessionRepository]. Plain constructor injection ([AppDatabase]) — no DI-framework
 * types leak in, so a later swap of Koin for another framework is a wiring-only change.
 *
 * Sync model: the **session is the sync unit**. Any change to its logged items or sets bumps the
 * parent [Session.updatedAt] and refreshes a SINGLE per-session outbox row (deterministic id), all
 * in one transaction — so the whole aggregate is pushed together and there's never a pile-up of
 * outbox rows for the same session.
 */
class SessionRepositoryImpl(
    private val database: AppDatabase,
    private val exerciseAssetReader: ExerciseAssetReader,
) : SessionRepository {

    private val sessions get() = database.sessionDao()
    private val outbox get() = database.outboxDao()
    private val loggedItems get() = database.loggedItemDao()
    private val setEntries get() = database.setEntryDao()
    private val exercises get() = database.exerciseDao()
    private val plans get() = database.plannedSessionDao()

    override fun observeSessions(): Flow<List<Session>> = sessions.observeAll()

    override fun observePlannedSession(): Flow<PlannedSession?> = plans.observeCurrent()

    override fun observeSession(sessionId: String): Flow<Session?> = sessions.observeById(sessionId)

    override fun observeLoggedItems(sessionId: String): Flow<List<LoggedItemWithSets>> =
        combine(
            loggedItems.observeForSession(sessionId),
            setEntries.observeForSession(sessionId),
        ) { items, sets ->
            val bySet = sets.groupBy { it.loggedItemId }
            items.map { item ->
                LoggedItemWithSets(item, bySet[item.id].orEmpty().sortedBy { it.setNumber })
            }
        }

    override fun observeVolumesBySession(): Flow<Map<String, Double>> =
        setEntries.observeSessionVolumes().map { list -> list.associate { it.sessionId to it.volume } }

    override fun observeExercisesWithHistory() = database.statsDao().exercisesWithHistory()

    override fun observeVolumeOverTime(exerciseId: String) =
        database.statsDao().volumeOverTime(exerciseId)

    override fun searchExercises(
        query: String,
        equipment: String?,
        muscle: String?,
    ): Flow<PagingData<Exercise>> =
        Pager(PagingConfig(pageSize = 30)) { exercises.search(query, equipment, muscle) }.flow

    override suspend fun exercisesById(): Map<String, Exercise> =
        exercises.getAll().associateBy { it.id }

    override suspend fun exerciseById(id: String): Exercise? = exercises.getById(id)

    override suspend fun createSession(type: String): Session =
        insertSession(name = displayName(type), type = type)

    override suspend fun startPlannedSession(plan: PlannedSession): Session =
        insertSession(name = plan.name, type = plan.type)

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun addExercise(sessionId: String, exerciseId: String) {
        val nextOrder = loggedItems.countForSession(sessionId)
        val item = LoggedItem(
            id = Uuid.random().toString(),
            sessionId = sessionId,
            exerciseId = exerciseId,
            orderIndex = nextOrder,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                loggedItems.insert(item)
                touchSession(sessionId)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun addSet(
        sessionId: String,
        loggedItemId: String,
        reps: Int?,
        loadKg: Double?,
        timeSec: Int?,
        distanceM: Int?,
    ) {
        val nextNumber = setEntries.getForLoggedItem(loggedItemId).size + 1
        val set = SetEntry(
            id = Uuid.random().toString(),
            loggedItemId = loggedItemId,
            setNumber = nextNumber,
            reps = reps,
            loadKg = loadKg,
            timeSec = timeSec,
            distanceM = distanceM,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.insert(set)
                touchSession(sessionId)
            }
        }
    }

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
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.insert(session)
                enqueueOutbox(session.id, now)
            }
        }
        return session
    }

    /** Marks a session dirty (new updatedAt + PENDING) and refreshes its single outbox row. */
    @OptIn(ExperimentalTime::class)
    private suspend fun touchSession(sessionId: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        val current = sessions.getById(sessionId) ?: return
        sessions.upsert(current.copy(updatedAt = now, syncStatus = SyncStatus.PENDING))
        enqueueOutbox(sessionId, now)
    }

    /** One outbox row per session (deterministic id) so repeated edits don't pile up rows. */
    private suspend fun enqueueOutbox(sessionId: String, now: Long) {
        outbox.upsert(
            OutboxEntry(
                id = "session:$sessionId",
                entityType = "session",
                entityId = sessionId,
                opType = "UPSERT",
                payload = "",
                createdAt = now,
            ),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun ensureSeeded() {
        if (exercises.count() == 0) {
            exercises.insertAll(ExerciseImporter.parse(exerciseAssetReader.readExercisesJson()))
        }
        if (plans.getCurrent() == null) {
            plans.upsert(
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

    private fun displayName(type: String): String = when (type) {
        SessionType.CONDITIONING -> "Conditioning"
        SessionType.HYROX -> "Hyrox"
        SessionType.MIXED -> "Mixed"
        else -> "Strength"
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun seedBenchmarkSessions(target: Int) {
        ensureSeeded() // exercise catalog must exist for the volume join
        val existing = sessions.count()
        if (existing >= target) return

        val now = Clock.System.now().toEpochMilliseconds()
        val dayMs = 86_400_000L
        val names = listOf("Upper Strength", "Lower Strength", "Push Day", "Pull Day", "Full Body")
        // One writer transaction for the whole batch — bulk insert is far faster than N transactions.
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                for (i in existing until target) {
                    val sessionId = Uuid.random().toString()
                    sessions.insert(
                        Session(
                            id = sessionId,
                            startedAt = now - i * dayMs,
                            name = names[i % names.size],
                            type = SessionType.STRENGTH,
                            updatedAt = now - i * dayMs,
                            syncStatus = SyncStatus.SYNCED,
                        ),
                    )
                    val itemId = Uuid.random().toString()
                    loggedItems.insert(LoggedItem(itemId, sessionId, "bench-press", 0))
                    repeat(3) { s ->
                        setEntries.insert(
                            SetEntry(
                                id = Uuid.random().toString(),
                                loggedItemId = itemId,
                                setNumber = s + 1,
                                reps = 8 + s,
                                loadKg = (40 + (i % 60)).toDouble(),
                            ),
                        )
                    }
                }
            }
        }
    }
}
