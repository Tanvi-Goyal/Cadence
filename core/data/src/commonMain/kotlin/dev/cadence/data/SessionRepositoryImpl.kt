package dev.cadence.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.cadence.common.UuidGenerator
import dev.cadence.domain.SessionRepository
import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.Block
import dev.cadence.data.local.Exercise as ExerciseEntity
import dev.cadence.data.local.ExerciseAssetReader
import dev.cadence.data.local.ExerciseEntry
import dev.cadence.data.local.ExerciseImporter
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.PersonalRecord as PersonalRecordEntity
import dev.cadence.data.local.Session as SessionEntity
import dev.cadence.data.local.SessionSource
import dev.cadence.data.local.SessionType
import dev.cadence.data.local.SetEntry as SetEntryEntity
import dev.cadence.data.local.SyncMeta
import dev.cadence.data.local.SyncMetaKeys
import dev.cadence.data.local.SyncStatus
import dev.cadence.data.local.TemplateSeed
import dev.cadence.domain.detectPrs
import dev.cadence.model.Exercise
import dev.cadence.model.PlannedSession
import dev.cadence.model.Session
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

    private companion object {
        /** Bump when [ExerciseImporter] output changes (v3 adds HyFit rows; v4 adds their muscles). */
        const val CATALOG_SEED_VERSION = 4

        /** Bump when [TemplateSeed] output changes (v2 adds per-day session goals). */
        const val TEMPLATE_SEED_VERSION = 2
    }

    private val sessions get() = database.sessionDao()
    private val outbox get() = database.outboxDao()
    private val blocks get() = database.blockDao()
    private val entries get() = database.exerciseEntryDao()
    private val setEntries get() = database.setEntryDao()
    private val exercises get() = database.exerciseDao()
    private val plans get() = database.plannedSessionDao()
    private val personalRecords get() = database.personalRecordDao()
    private val syncMeta get() = database.syncMetaDao()

    /**
     * Recompute + upsert PBs for a just-written set, inside the caller's write transaction. Pure
     * [detectPrs] decides the winners; here we assign ids/timestamps, reusing the existing row of a
     * kind/bucket so a record updates in place rather than piling up. Template (target-only) sets
     * yield no candidates, so this is a no-op for them.
     */
    private suspend fun detectAndStorePrs(set: SetEntry, now: Long) {
        val entry = entries.getById(set.exerciseEntryId) ?: return
        val exercise = exercises.getById(entry.exerciseId)?.toDomain() ?: return
        val current = personalRecords.getForExercise(entry.exerciseId).map { it.toDomain() }
        detectPrs(exercise, set, current).forEach { candidate ->
            val existing = current.firstOrNull {
                it.kind == candidate.kind && it.distanceBucketM == candidate.distanceBucketM
            }
            personalRecords.upsert(
                PersonalRecordEntity(
                    id = existing?.id ?: uuid.newId(),
                    exerciseId = entry.exerciseId,
                    kind = candidate.kind.name,
                    value = candidate.value,
                    distanceBucketM = candidate.distanceBucketM,
                    achievedAt = now,
                    sourceSetId = set.id,
                    createdAt = existing?.createdAt?.toEpochMilliseconds() ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    /** Every session currently has one deterministic implicit STRAIGHT block; entries hang off it. */
    private fun implicitBlock(sessionId: String, now: Long): Block = Block(
        id = implicitBlockId(sessionId),
        sessionId = sessionId,
        type = "STRAIGHT", // dev.cadence.model.BlockType.STRAIGHT
        orderIndex = 0,
        rounds = 1,
        createdAt = now,
        updatedAt = now,
    )

    override fun observeSessions(): Flow<List<Session>> =
        sessions.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeTemplates(): Flow<List<Session>> =
        sessions.observeTemplates().map { rows -> rows.map { it.toDomain() } }

    override fun observePlannedSession(): Flow<PlannedSession?> =
        plans.observeCurrent().map { it?.toDomain() }

    override fun observeSessionDetail(sessionId: String): Flow<SessionDetail?> =
        combine(
            sessions.observeById(sessionId),
            blocks.observeForSession(sessionId),
            entries.observeBySession(sessionId),
            setEntries.observeForSession(sessionId),
        ) { session, sessionBlocks, sessionEntries, sets ->
            session?.let {
                buildSessionDetail(it,
                    sessionBlocks,
                    sessionEntries,
                    sets,
                    resolveExercises(sessionEntries))
            }
        }

    /**
     * Resolve just the catalog rows a session references (an indexed `IN` lookup, not the full
     * ~870-row catalog), as domain models. Re-runs per emission — cheap for a handful of exercises;
     * revisit with a cache if a session ever references many.
     */
    private suspend fun resolveExercises(items: List<ExerciseEntry>): Map<String, Exercise> =
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
        Pager(PagingConfig(pageSize = 30)) { exercises.search(query, equipment, muscle) }
            .flow.map { page -> page.map { it.toDomain() } }

    override suspend fun exercisesById(): Map<String, Exercise> =
        exercises.getAll().associate { it.id to it.toDomain() }

    override suspend fun exerciseById(id: String): Exercise? = exercises.getById(id)?.toDomain()

    override suspend fun createSession(type: String): Session =
        insertSession(name = displayName(type), type = type).toDomain()

    override suspend fun createTemplate(name: String, type: String): Session =
        insertSession(name = name, type = type, isTemplate = true, source = SessionSource.MANUAL).toDomain()

    override suspend fun startPlannedSession(plan: PlannedSession): Session =
        insertSession(name = plan.name, type = plan.type.name).toDomain()

    override suspend fun addExercise(sessionId: String, exerciseId: String) {
        val now = now()
        val blockId = implicitBlockId(sessionId)
        val nextOrder = entries.countForBlock(blockId)
        val entry = ExerciseEntry(
            id = uuid.newId(),
            blockId = blockId,
            exerciseId = exerciseId,
            orderIndex = nextOrder,
            createdAt = now,
            updatedAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                blocks.insert(implicitBlock(sessionId, now)) // insert-if-absent (IGNORE on conflict)
                entries.insert(entry)
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
        val now = now()
        val nextNumber = setEntries.getForEntry(loggedItemId).size + 1
        val set = SetEntryEntity(
            id = uuid.newId(),
            exerciseEntryId = loggedItemId,
            setNumber = nextNumber,
            reps = reps,
            loadKg = loadKg,
            timeSec = timeSec,
            distanceM = distanceM,
            createdAt = now,
            updatedAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.insert(set)
                detectAndStorePrs(set.toDomain(), now)
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
        val now = now()
        val nextNumber = setEntries.getForEntry(loggedItemId).size + 1
        val set = SetEntryEntity(
            id = uuid.newId(),
            exerciseEntryId = loggedItemId,
            setNumber = nextNumber,
            targetReps = reps,
            targetLoadKg = loadKg,
            targetTimeSec = timeSec,
            targetDistanceM = distanceM,
            createdAt = now,
            updatedAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.insert(set)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun updateSet(sessionId: String, set: SetEntry) {
        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.update(set.toEntity())
                detectAndStorePrs(set, now)
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
    ): SessionEntity {
        val now = now()
        val session = SessionEntity(
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
        val newSession = SessionEntity(
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

        // Deep-copy the WHOLE block tree with FRESH ids, built outside the write transaction so the
        // writer lock is held only for inserts. Each template block is copied (carrying its section /
        // conditioning shape), entries are reparented under their copied block, and each set's targets
        // carry over while actuals stay null → the UI shows the prescription as ghost values until the
        // user logs what actually happened.
        val templateBlocks = blocks.getBySession(templateId)
        val newBlockIdByOld = templateBlocks.associate { it.id to uuid.newId() }
        val newBlocks = templateBlocks.map { tb ->
            Block(
                id = newBlockIdByOld.getValue(tb.id),
                sessionId = newSession.id,
                type = tb.type,
                orderIndex = tb.orderIndex,
                rounds = tb.rounds,
                restBetweenRoundsMs = tb.restBetweenRoundsMs,
                label = tb.label,
                section = tb.section,
                conditioningFormat = tb.conditioningFormat,
                capSeconds = tb.capSeconds,
                workSeconds = tb.workSeconds,
                createdAt = now,
                updatedAt = now,
            )
        }
        val entriesWithSets = entries.getBySession(templateId).map { entry ->
            val newEntryId = uuid.newId()
            val newEntry = ExerciseEntry(
                id = newEntryId,
                blockId = newBlockIdByOld.getValue(entry.blockId),
                exerciseId = entry.exerciseId,
                orderIndex = entry.orderIndex,
                targetSets = entry.targetSets,
                restMs = entry.restMs,
                note = entry.note,
                eachSide = entry.eachSide,
                createdAt = now,
                updatedAt = now,
            )
            val newSets = setEntries.getForEntry(entry.id).map { set ->
                SetEntryEntity(
                    id = uuid.newId(),
                    exerciseEntryId = newEntryId,
                    setNumber = set.setNumber,
                    targetReps = set.targetReps,
                    targetLoadKg = set.targetLoadKg,
                    targetTimeSec = set.targetTimeSec,
                    targetDistanceM = set.targetDistanceM,
                    targetCalories = set.targetCalories,
                    createdAt = now,
                    updatedAt = now,
                )
            }
            newEntry to newSets
        }

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.insert(newSession)
                newBlocks.forEach { blocks.insert(it) }
                entriesWithSets.forEach { (entry, sets) ->
                    entries.insert(entry)
                    sets.forEach { setEntries.insert(it) }
                }
                enqueueOutbox(newSession.id, now)
            }
        }
        return newSession.toDomain()
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
        // Versioned re-seed: fresh installs import; existing installs whose catalog predates the
        // current seed (e.g. before modality/Hyrox landed) upsert to refresh + add the new rows.
        val seeded = syncMeta.get(SyncMetaKeys.SEED_VERSION)?.toIntOrNull() ?: 0
        if (seeded < CATALOG_SEED_VERSION) {
            val catalog = ExerciseImporter.parse(exerciseAssetReader.readExercisesJson()) +
                ExerciseImporter.supplementalSeed()
            exercises.upsertAll(catalog)
            syncMeta.set(SyncMeta(SyncMetaKeys.SEED_VERSION, CATALOG_SEED_VERSION.toString()))
        }

        // Program templates (multi-block day trees, target-only sets). Seeded local reference data —
        // like the catalog, they carry no outbox row. Fixed ids → an idempotent clear-then-insert.
        val templateSeeded = syncMeta.get(SyncMetaKeys.TEMPLATE_SEED_VERSION)?.toIntOrNull() ?: 0
        if (templateSeeded < TEMPLATE_SEED_VERSION) {
            val templates = TemplateSeed.all(now())
            database.useWriterConnection { connection ->
                connection.immediateTransaction {
                    templates.forEach { t ->
                        val entryIds = entries.getBySession(t.session.id).map { it.id }
                        if (entryIds.isNotEmpty()) setEntries.deleteForEntries(entryIds)
                        entries.deleteBySession(t.session.id) // before blocks (its subquery joins blocks)
                        blocks.deleteBySession(t.session.id)
                        sessions.upsert(t.session)
                        t.blocks.forEach { blocks.insert(it) }
                        t.entries.forEach { entries.insert(it) }
                        t.sets.forEach { setEntries.insert(it) }
                    }
                }
            }
            syncMeta.set(SyncMeta(SyncMetaKeys.TEMPLATE_SEED_VERSION, TEMPLATE_SEED_VERSION.toString()))
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
                        SessionEntity(
                            id = sessionId,
                            startedAt = now - i * dayMs,
                            name = names[i % names.size],
                            type = SessionType.STRENGTH,
                            createdAt = now - i * dayMs,
                            updatedAt = now - i * dayMs,
                            syncStatus = SyncStatus.SYNCED,
                        ),
                    )
                    val ts = now - i * dayMs
                    blocks.insert(implicitBlock(sessionId, ts))
                    val entryId = uuid.newId()
                    entries.insert(
                        ExerciseEntry(
                            id = entryId, blockId = implicitBlockId(sessionId), exerciseId = "bench-press",
                            orderIndex = 0, createdAt = ts, updatedAt = ts,
                        ),
                    )
                    repeat(3) { s ->
                        setEntries.insert(
                            SetEntryEntity(
                                id = uuid.newId(),
                                exerciseEntryId = entryId,
                                setNumber = s + 1,
                                reps = 8 + s,
                                loadKg = (40 + (i % 60)).toDouble(),
                                createdAt = ts,
                                updatedAt = ts,
                            ),
                        )
                    }
                }
            }
        }
    }
}
