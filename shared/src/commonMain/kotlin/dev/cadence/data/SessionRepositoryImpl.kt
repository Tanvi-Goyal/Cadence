package dev.cadence.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.common.UuidGenerator
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.Exercise
import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.ExerciseImporter
import dev.cadence.data.local.LoggedItem
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import dev.cadence.data.local.SessionSource
import dev.cadence.data.local.SessionType
import dev.cadence.data.local.SetEntry as SetEntryEntity
import dev.cadence.data.local.SyncStatus
import dev.cadence.model.SessionDetail
import dev.cadence.model.SetEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Room-backed [SessionRepository]. Plain constructor injection ([AppDatabase]) — no DI-framework
 * types leak in, so a later swap of Koin for another framework is a wiring-only change.
 *
 * Sync model: the **session is the sync unit**. Any change to its logged items or sets bumps the
 * parent [Session.updatedAt] and refreshes a SINGLE per-session outbox row (deterministic id), all
 * in one transaction — so the whole aggregate is pushed together and there's never a pile-up of
 * outbox rows for the same session.
 */
@OptIn(ExperimentalTime::class)
class SessionRepositoryImpl(
    private val database: AppDatabase,
    private val exerciseAssetReader: ExerciseAssetReader,
    private val uuid: UuidGenerator,
    private val clock: Clock,
) : SessionRepository {

    /** Wall-clock millis via the injected [clock] — the single time source for all writes. */
    private fun now(): Long = clock.now().toEpochMilliseconds()

    private val sessions get() = database.sessionDao()
    private val outbox get() = database.outboxDao()
    private val loggedItems get() = database.loggedItemDao()
    private val setEntries get() = database.setEntryDao()
    private val exercises get() = database.exerciseDao()
    private val plans get() = database.plannedSessionDao()

    override fun observeSessions(): Flow<List<Session>> = sessions.observeAll()

    override fun observeTemplates(): Flow<List<Session>> = sessions.observeTemplates()

    override fun observePlannedSession(): Flow<PlannedSession?> = plans.observeCurrent()

    override fun observeSessionDetail(sessionId: String): Flow<SessionDetail?> =
        combine(
            sessions.observeById(sessionId),
            loggedItems.observeForSession(sessionId),
            setEntries.observeForSession(sessionId),
        ) { session, items, sets ->
            session?.let { buildSessionDetail(it, items, sets, resolveExercises(items)) }
        }

    /**
     * Resolve just the catalog rows a session references (an indexed `IN` lookup, not the full
     * ~870-row catalog), as domain models. Re-runs per emission — cheap for a handful of exercises;
     * revisit with a cache if a session ever references many.
     */
    private suspend fun resolveExercises(items: List<LoggedItem>): Map<String, dev.cadence.model.Exercise> =
        exercises.getByIds(items.map { it.exerciseId }.distinct()).associate { it.id to it.toDomain() }

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

    override suspend fun createTemplate(name: String, type: String): Session =
        insertSession(name = name, type = type, isTemplate = true, source = SessionSource.MANUAL)

    override suspend fun startPlannedSession(plan: PlannedSession): Session =
        insertSession(name = plan.name, type = plan.type)

    override suspend fun addExercise(sessionId: String, exerciseId: String) {
        val nextOrder = loggedItems.countForSession(sessionId)
        val item = LoggedItem(
            id = uuid.newId(),
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

    override suspend fun addSet(
        sessionId: String,
        loggedItemId: String,
        reps: Int?,
        loadKg: Double?,
        timeSec: Int?,
        distanceM: Int?,
    ) {
        val nextNumber = setEntries.getForLoggedItem(loggedItemId).size + 1
        val set = SetEntryEntity(
            id = uuid.newId(),
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

    override suspend fun addTargetSet(
        sessionId: String,
        loggedItemId: String,
        reps: Int?,
        loadKg: Double?,
        timeSec: Int?,
        distanceM: Int?,
    ) {
        val nextNumber = setEntries.getForLoggedItem(loggedItemId).size + 1
        val set = SetEntryEntity(
            id = uuid.newId(),
            loggedItemId = loggedItemId,
            setNumber = nextNumber,
            targetReps = reps,
            targetLoadKg = loadKg,
            targetTimeSec = timeSec,
            targetDistanceM = distanceM,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.insert(set)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun updateSet(sessionId: String, set: SetEntry) {
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.update(set.toEntity())
                touchSession(sessionId)
            }
        }
    }

    private suspend fun insertSession(
        name: String,
        type: String,
        isTemplate: Boolean = false,
        source: String = SessionSource.MANUAL,
        templateId: String? = null,
    ): Session {
        val now = now()
        val session = Session(
            id = uuid.newId(),
            startedAt = now,
            name = name,
            type = type,
            isTemplate = isTemplate,
            source = source,
            templateId = templateId,
            createdAt = now,
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

    override suspend fun instantiateTemplate(templateId: String): Session {
        val template = sessions.getById(templateId)
            ?: throw IllegalArgumentException("No template with id $templateId")
        require(template.isTemplate) { "Session $templateId is not a template" }

        val now = now()
        val newSession = Session(
            id = uuid.newId(),
            startedAt = now,
            name = template.name,
            type = template.type,
            notes = template.notes,
            isTemplate = false,
            source = SessionSource.FROM_TEMPLATE,
            templateId = templateId, // provenance — this session was spawned from that template
            createdAt = now,
            updatedAt = now,
        )

        // Deep-copy the tree with FRESH ids, built outside the write transaction so the writer lock
        // is held only for inserts. Each set's targets are carried over; actuals stay null → the UI
        // shows the prescription as ghost values until the user logs what actually happened.
        val itemsWithSets = loggedItems.getBySession(templateId).map { item ->
            val newItem = LoggedItem(
                id = uuid.newId(),
                sessionId = newSession.id,
                exerciseId = item.exerciseId,
                orderIndex = item.orderIndex,
            )
            val newSets = setEntries.getForLoggedItem(item.id).map { set ->
                SetEntryEntity(
                    id = uuid.newId(),
                    loggedItemId = newItem.id,
                    setNumber = set.setNumber,
                    targetReps = set.targetReps,
                    targetLoadKg = set.targetLoadKg,
                    targetTimeSec = set.targetTimeSec,
                    targetDistanceM = set.targetDistanceM,
                )
            }
            newItem to newSets
        }

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.insert(newSession)
                itemsWithSets.forEach { (item, sets) ->
                    loggedItems.insert(item)
                    sets.forEach { setEntries.insert(it) }
                }
                enqueueOutbox(newSession.id, now)
            }
        }
        return newSession
    }

    /** Marks a session dirty (new updatedAt + PENDING) and refreshes its single outbox row. */
    private suspend fun touchSession(sessionId: String) {
        val now = now()
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

    override suspend fun ensureSeeded() {
        if (exercises.count() == 0) {
            exercises.insertAll(ExerciseImporter.parse(exerciseAssetReader.readExercisesJson()))
        }
        if (plans.getCurrent() == null) {
            plans.upsert(
                PlannedSession(
                    id = uuid.newId(),
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

    override suspend fun seedBenchmarkSessions(target: Int) {
        ensureSeeded() // exercise catalog must exist for the volume join
        val existing = sessions.count()
        if (existing >= target) return

        val now = now()
        val dayMs = 86_400_000L
        val names = listOf("Upper Strength", "Lower Strength", "Push Day", "Pull Day", "Full Body")
        // One writer transaction for the whole batch — bulk insert is far faster than N transactions.
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                for (i in existing until target) {
                    val sessionId = uuid.newId()
                    sessions.insert(
                        Session(
                            id = sessionId,
                            startedAt = now - i * dayMs,
                            name = names[i % names.size],
                            type = SessionType.STRENGTH,
                            createdAt = now - i * dayMs,
                            updatedAt = now - i * dayMs,
                            syncStatus = SyncStatus.SYNCED,
                        ),
                    )
                    val itemId = uuid.newId()
                    loggedItems.insert(LoggedItem(itemId, sessionId, "bench-press", 0))
                    repeat(3) { s ->
                        setEntries.insert(
                            SetEntryEntity(
                                id = uuid.newId(),
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
