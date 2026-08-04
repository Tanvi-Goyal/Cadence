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
import dev.cadence.data.local.EventDivisionEntity
import dev.cadence.data.local.EventSeed
import dev.cadence.data.local.EventSegmentEntity
import dev.cadence.data.local.ExerciseEntry
import dev.cadence.data.local.ExerciseImporter
import dev.cadence.data.local.OutboxEntry
import dev.cadence.data.local.SegmentStandardEntity
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
import dev.cadence.model.EventFormat
import dev.cadence.model.Exercise
import dev.cadence.model.HyroxDivisionInfo
import dev.cadence.model.HyroxStation
import dev.cadence.model.HyroxStepDef
import dev.cadence.model.HyroxStepKind
import dev.cadence.model.HyroxVariant
import dev.cadence.model.MetricType
import dev.cadence.model.PlannedSession
import dev.cadence.model.RaceMode
import dev.cadence.model.SegmentKind
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
        /**
         * Bump when [ExerciseImporter] output changes (v3 adds HyFit rows; v4 adds their muscles;
         * v5 adds the `hyrox-run` leg).
         */
        const val CATALOG_SEED_VERSION = 5

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
    private val eventRef get() = database.eventRefDao()

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

    // ── HYROX live workout ──────────────────────────────────────────────────────────────────────

    override suspend fun hyroxDivisions(): List<HyroxDivisionInfo> {
        ensureSeeded()
        return eventRef.divisionsForFormat(EventFormat.HYROX).map { HyroxDivisionInfo(it.key, it.label) }
    }

    override suspend fun hyroxFormat(divisionKey: String, variant: HyroxVariant): List<HyroxStepDef> {
        ensureSeeded()
        val format = EventFormat.HYROX
        val segments = eventRef.segmentsForFormat(format)
        val standards = eventRef.standardsFor(format, divisionKey, RaceMode.SINGLES.name)
            .associateBy { it.segmentId }
        // Resolve each station's HyroxStation enum from its catalog exercise (data-driven, not hardcoded).
        val stationEnumByExercise = exercises.getByIds(segments.map { it.exerciseId }.distinct())
            .associate { it.id to it.hyroxStation?.let { name -> HyroxStation.entries.firstOrNull { e -> e.name == name } } }

        // A Hyrox format resolves to 16 ordered segments (run, station, …). Variants slice by order:
        // first half = segments 1..8 (stations 1–4 + runs), second half = 9..16; HALVED keeps every
        // segment but halves each distance/rep.
        val selected = when (variant) {
            HyroxVariant.FIRST_HALF -> segments.filter { it.orderIndex in 1..8 }
            HyroxVariant.SECOND_HALF -> segments.filter { it.orderIndex in 9..16 }
            HyroxVariant.FULL, HyroxVariant.HALVED -> segments
        }
        val halve = variant == HyroxVariant.HALVED

        // Re-index 0..n over the SELECTED segments so a variant's first step is index 0 (the timer
        // advances by index, and recordHyroxSplit finds the entry by orderIndex == index).
        return selected.mapIndexed { i, seg ->
            segmentStep(i, seg, stationEnumByExercise[seg.exerciseId], standards[seg.id], halve)
        }
    }

    private fun segmentStep(
        index: Int,
        seg: EventSegmentEntity,
        station: HyroxStation?,
        standard: SegmentStandardEntity?,
        halve: Boolean,
    ): HyroxStepDef {
        if (seg.kind == SegmentKind.RUN.name) {
            val dist = (seg.distanceM ?: 1000).let { if (halve) it / 2 else it }
            return HyroxStepDef(
                index = index, kind = HyroxStepKind.RUN, station = null, exerciseId = seg.exerciseId,
                title = seg.name, detail = seg.descriptor, value = "${dist / 1000.0} km",
                targetDistanceM = dist, segmentKey = seg.id,
            )
        }
        val stationNumber = seg.orderIndex / 2 // stations sit at even order 2,4,…16 → 1..8
        val title = "$stationNumber. ${seg.name}"
        // Rep-scored station (wall balls): reps + ball weight/target from the division standard.
        if (seg.metric == MetricType.REPS_ONLY.name) {
            val reps = (standard?.targetReps ?: seg.reps ?: 100).let { if (halve) it / 2 else it }
            return HyroxStepDef(
                index, HyroxStepKind.STATION, station, seg.exerciseId, title,
                detail = "${standard?.loadKg?.toInt() ?: 0}kg Ball • ${standard?.targetHeightM ?: "-"}m Target",
                value = "$reps reps", targetReps = reps, targetLoadKg = standard?.loadKg, segmentKey = seg.id,
            )
        }
        val dist = (seg.distanceM ?: 0).let { if (halve) it / 2 else it }
        // Loaded station shows the rulebook weight; an unloaded/erg station shows pure distance.
        return if (standard?.loadKg != null) {
            HyroxStepDef(
                index, HyroxStepKind.STATION, station, seg.exerciseId, title,
                detail = "${dist}m ${seg.descriptor}", value = standard.loadDisplay ?: "",
                targetDistanceM = dist, targetLoadKg = standard.loadKg, segmentKey = seg.id,
            )
        } else {
            HyroxStepDef(
                index, HyroxStepKind.STATION, station, seg.exerciseId, title,
                detail = "${dist}m ${seg.descriptor}", value = "${dist}m",
                targetDistanceM = dist, segmentKey = seg.id,
            )
        }
    }

    override suspend fun startHyroxSession(divisionKey: String, variant: HyroxVariant, templateId: String): String {
        val steps = hyroxFormat(divisionKey, variant)
        val now = now()
        val sessionId = uuid.newId()
        val session = SessionEntity(
            id = sessionId, startedAt = now, name = "Hyrox", type = SessionType.HYROX,
            isTemplate = false, source = SessionSource.RACE_SIM, templateId = templateId,
            formatKey = EventFormat.HYROX, divisionKey = divisionKey,
            createdAt = now, updatedAt = now,
        )
        val block = implicitBlock(sessionId, now)
        // One entry+set per step; the set carries the step's TARGETS (actuals filled in as splits land).
        // The entry is tagged with the segment it fulfils so the race timeline / per-segment PBs are
        // ordered reads, not inference.
        val rows = steps.map { step ->
            val entryId = uuid.newId()
            val entry = ExerciseEntry(
                id = entryId, blockId = block.id, exerciseId = step.exerciseId,
                orderIndex = step.index, note = step.title, segmentKey = step.segmentKey,
                createdAt = now, updatedAt = now,
            )
            val set = SetEntryEntity(
                id = uuid.newId(), exerciseEntryId = entryId, setNumber = 1,
                targetDistanceM = step.targetDistanceM, targetReps = step.targetReps,
                targetLoadKg = step.targetLoadKg, createdAt = now, updatedAt = now,
            )
            entry to set
        }
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.insert(session)
                blocks.insert(block)
                rows.forEach { (entry, set) ->
                    entries.insert(entry)
                    setEntries.insert(set)
                }
                enqueueOutbox(sessionId, now)
            }
        }
        return sessionId
    }

    override suspend fun recordHyroxSplit(sessionId: String, stepIndex: Int, elapsedSec: Int) {
        val entry = entries.getBySession(sessionId).firstOrNull { it.orderIndex == stepIndex } ?: return
        val set = setEntries.getForEntry(entry.id).firstOrNull() ?: return
        val now = now()
        val updated = set.copy(
            timeSec = elapsedSec,
            distanceM = set.targetDistanceM ?: set.distanceM, // record the full distance for pace
            updatedAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.update(updated)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun finishSession(sessionId: String) {
        val current = sessions.getById(sessionId) ?: return
        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.upsert(current.copy(finishedAt = now, updatedAt = now, syncStatus = SyncStatus.PENDING))
                enqueueOutbox(sessionId, now)
            }
        }
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

        // Event-format reference tables (formats / segments / divisions / per-division standards).
        // Reference data, like the catalog — no outbox. Fixed slug ids → idempotent upsert. Adding a
        // new event (DEKA/CrossFit) is a seed change here + a VERSION bump, never a migration.
        val eventSeeded = syncMeta.get(SyncMetaKeys.EVENT_SEED_VERSION)?.toIntOrNull() ?: 0
        if (eventSeeded < EventSeed.VERSION) {
            eventRef.upsertFormats(EventSeed.formats)
            eventRef.upsertSegments(EventSeed.segments)
            eventRef.upsertDivisions(EventSeed.divisions)
            eventRef.upsertStandards(EventSeed.standards)
            syncMeta.set(SyncMeta(SyncMetaKeys.EVENT_SEED_VERSION, EventSeed.VERSION.toString()))
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
