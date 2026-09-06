package com.mindset.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.mindset.common.UuidGenerator
import com.mindset.data.local.AppDatabase
import com.mindset.data.local.BlockEntity
import com.mindset.data.local.EventSeed
import com.mindset.data.local.EventSegmentEntity
import com.mindset.data.local.EventSegmentStandardEntity
import com.mindset.data.local.ExerciseAssetReader
import com.mindset.data.local.ExerciseEntryEntity
import com.mindset.data.local.ExerciseImporter
import com.mindset.data.local.OutboxEntry
import com.mindset.data.local.SessionEntity
import com.mindset.data.local.SessionSource
import com.mindset.data.local.SessionType
import com.mindset.data.local.SetEntryEntity
import com.mindset.data.local.SyncMeta
import com.mindset.data.local.SyncMetaKeys
import com.mindset.data.local.SyncStatus
import com.mindset.data.local.TemplateSeed
import com.mindset.domain.detectPrs
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.EventFormat
import com.mindset.model.Exercise
import com.mindset.model.Gender
import com.mindset.model.HyroxStation
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxStationType
import com.mindset.model.HyroxVariant
import com.mindset.model.MetricType
import com.mindset.model.PlannedSession
import com.mindset.model.RaceMode
import com.mindset.model.SegmentKind
import com.mindset.model.Session
import com.mindset.model.SessionDetail
import com.mindset.model.SetEntry
import com.mindset.model.StationAggregate
import com.mindset.model.StationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.mindset.data.local.PersonalRecord as PersonalRecordEntity
import com.mindset.model.SessionType as DomainSessionType

@OptIn(ExperimentalTime::class)
class SessionRepositoryImpl(
    private val database: AppDatabase,
    private val exerciseAssetReader: ExerciseAssetReader,
    private val uuid: UuidGenerator,
    private val clock: Clock,
) : SessionRepository {

    private fun now(): Long = clock.now().toEpochMilliseconds()

    private companion object {
        const val CATALOG_SEED_VERSION = 6
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
    private val eventSegmentDao get() = database.eventSegmentDao()
    private val eventSegmentStandardDao get() = database.eventSegmentStandardDao()
    private val eventFormatDao get() = database.eventFormatDao()
    private val eventDivisionDao get() = database.eventDivisionDao()

    override fun observeSessionDetail(sessionId: String): Flow<SessionDetail?> = combine(
        sessions.observeById(sessionId),
        blocks.observeForSession(sessionId),
        entries.observeBySession(sessionId),
        setEntries.observeForSession(sessionId),
    ) { session, sessionBlocks, sessionEntries, sets ->
        session?.let {
            buildSessionDetail(
                it,
                sessionBlocks,
                sessionEntries,
                sets,
                resolveExercises(sessionEntries),
            )
        }
    }

    /**
     * Recompute + upsert PBs for a just-written set, inside the caller's write transaction. Pure
     * [detectPrs] decides the winners; here we assign ids/timestamps, reusing the existing row of a
     * kind/bucket so a record updates in place rather than piling up. Template (target-only) sets
     * yield no candidates, so this is a no-op for them.
     */
    private suspend fun detectAndStorePrs(sessionId: String, set: SetEntry, now: Long) {
        val entry = entries.getById(set.exerciseEntryId) ?: return
        val exercise = exercises.getById(entry.exerciseId)?.toDomain() ?: return

        val divisionKey = sessions.getById(sessionId)?.divisionKey
        val current = personalRecords.getForExercise(entry.exerciseId).map { it.toDomain() }
        detectPrs(exercise, set, current, divisionKey).forEach { candidate ->
            val existing = current.firstOrNull {
                it.kind == candidate.kind &&
                    it.distanceBucketM == candidate.distanceBucketM &&
                    it.divisionKey == candidate.divisionKey
            }

            personalRecords.upsert(
                PersonalRecordEntity(
                    id = existing?.id ?: uuid.newId(),
                    exerciseId = entry.exerciseId,
                    kind = candidate.kind.name,
                    value = candidate.value,
                    distanceBucketM = candidate.distanceBucketM,
                    divisionKey = candidate.divisionKey,
                    achievedAt = now,
                    sourceSetId = set.id,
                    createdAt = existing?.createdAt?.toEpochMilliseconds() ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    /** Every session currently has one deterministic implicit STRAIGHT block; entries hang off it. */
    private fun implicitBlock(sessionId: String, now: Long): BlockEntity = BlockEntity(
        id = implicitBlockId(sessionId),
        sessionId = sessionId,
        type = "STRAIGHT",
        orderIndex = 0,
        rounds = 1,
        createdAt = now,
        updatedAt = now,
    )

    override fun observeSessions(): Flow<List<Session>> = sessions.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeStationRecords(): Flow<List<StationRecord>> =
        personalRecords.observeHyroxRecords().map { rows -> rows.mapNotNull { it.toStationRecord() } }

    /** `exercises.hyroxStation` is stored as the enum name; same idiom [hyroxFormat] uses. */
    private fun stationOf(name: String): HyroxStation? = HyroxStation.entries.firstOrNull { it.name == name }

    override fun observeStationAggregates(divisionKey: String): Flow<Map<HyroxStation, StationAggregate>> =
        combine(
            setEntries.observeStationSessionCounts(divisionKey),
            setEntries.observeStationRecents(divisionKey),
        ) { counts, recents ->
            // `observeStationRecents` is ordered newest-first per station, so the first row of each
            // group is that station's most recent split — no sorting or MAX-bare-column trickery here.
            // Collapsing to one row per session first is what makes "previous" mean the previous
            // *session* rather than the previous set of the same workout.
            val splitsByStation = recents
                .groupBy { it.hyroxStation }
                .mapNotNull { (key, rows) ->
                    stationOf(key)?.to(rows.distinctBy { it.sessionId }.map { it.timeSec })
                }
                .toMap()
            val countByStation = counts
                .mapNotNull { row -> stationOf(row.hyroxStation)?.to(row.sessionCount) }
                .toMap()

            (splitsByStation.keys + countByStation.keys).associateWith { station ->
                val splits = splitsByStation[station].orEmpty()
                StationAggregate(
                    recentTimeSec = splits.firstOrNull(),
                    previousTimeSec = splits.getOrNull(1),
                    sessionCount = countByStation[station] ?: 0,
                )
            }
        }

    override fun observeRecentSessions(limit: Int): Flow<List<Session>> =
        sessions.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }

    override fun observeSessionsSince(startMillis: Long): Flow<List<Session>> =
        sessions.observeSince(startMillis).map { rows -> rows.map { it.toDomain() } }

    override fun observeTemplates(): Flow<List<Session>> = sessions.observeTemplates().map { rows -> rows.map { it.toDomain() } }

    override fun observePlannedSession(): Flow<PlannedSession?> = plans.observeCurrent().map { it?.toDomain() }

    /**
     * Resolve just the catalog rows a session references (an indexed `IN` lookup, not the full
     * ~870-row catalog), as domain models. Re-runs per emission — cheap for a handful of exercises;
     * revisit with a cache if a session ever references many.
     */
    private suspend fun resolveExercises(items: List<ExerciseEntryEntity>): Map<String, Exercise> =
        exercises.getByIds(items.map { it.exerciseId }.distinct())
            .associate { it.id to it.toDomain() }

    override fun observeVolumesBySession(): Flow<Map<String, Double>> = setEntries.observeSessionVolumes()
        .map { list -> list.associate { it.sessionId to it.volume } }

    override fun observeDurationsBySession(): Flow<Map<String, Int>> = setEntries.observeSessionDurations()
        .map { list -> list.associate { it.sessionId to it.durationSec } }

    override fun observeExercisesWithHistory() = database.statsDao().exercisesWithHistory()

    override fun observeVolumeOverTime(exerciseId: String) = database.statsDao().volumeOverTime(exerciseId)

    override fun searchExercises(query: String, equipment: String?, muscle: String?): Flow<PagingData<Exercise>> =
        Pager(PagingConfig(pageSize = 30)) { exercises.search(query, equipment, muscle) }
            .flow.map { page -> page.map { it.toDomain() } }

    override fun pagedSessions(type: DomainSessionType?): Flow<PagingData<Session>> =
        Pager(PagingConfig(pageSize = 20)) { sessions.pagedSessions(type?.name) }
            .flow.map { page -> page.map { it.toDomain() } }

    override suspend fun exercisesById(): Map<String, Exercise> = exercises.getAll().associate { it.id to it.toDomain() }

    override suspend fun exerciseById(id: String): Exercise? = exercises.getById(id)?.toDomain()

    override suspend fun createSession(type: String): Session = insertSession(name = displayName(type), type = type).toDomain()

    override suspend fun resumeOrCreateSession(type: String): Session =
        sessions.latestUnfinishedManual()?.toDomain() ?: createSession(type)

    override suspend fun createTemplate(name: String, type: String): Session = insertSession(
        name = name,
        type = type,
        isTemplate = true,
        source = SessionSource.MANUAL,
    ).toDomain()

    override suspend fun startPlannedSession(plan: PlannedSession): Session = insertSession(name = plan.name, type = plan.type.name).toDomain()

    override suspend fun addExercise(sessionId: String, exerciseId: String) {
        val now = now()
        val blockId = implicitBlockId(sessionId)
        val nextOrder = entries.countForBlock(blockId)
        val entry = ExerciseEntryEntity(
            id = uuid.newId(),
            blockId = blockId,
            exerciseId = exerciseId,
            orderIndex = nextOrder,
            createdAt = now,
            updatedAt = now,
        )

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                blocks.insert(
                    implicitBlock(
                        sessionId,
                        now,
                    ),
                ) // insert-if-absent (IGNORE on conflict)
                entries.insert(entry)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun addExercisePrefilled(sessionId: String, exerciseId: String) {
        val now = now()
        val blockId = implicitBlockId(sessionId)
        val nextOrder = entries.countForBlock(blockId)
        val entryId = uuid.newId()
        val entry = ExerciseEntryEntity(
            id = entryId,
            blockId = blockId,
            exerciseId = exerciseId,
            orderIndex = nextOrder,
            createdAt = now,
            updatedAt = now,
        )
        // Read the last-performed sets (a bounded read, done before opening the writer lock) and
        // recreate them as ghost TARGETS — actuals stay null so the UI renders them as editable
        // prefills to confirm/adjust. No history → no seeded sets (the add-set row captures the first).
        val ghostSets = setEntries.lastSetsForExercise(exerciseId, sessionId).mapIndexed { i, s ->
            SetEntryEntity(
                id = uuid.newId(),
                exerciseEntryId = entryId,
                setNumber = i + 1,
                targetReps = s.reps,
                targetLoadKg = s.loadKg,
                targetTimeSec = s.timeSec,
                targetDistanceM = s.distanceM,
                targetCalories = s.calories,
                createdAt = now,
                updatedAt = now,
            )
        }
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                blocks.insert(
                    implicitBlock(
                        sessionId,
                        now,
                    ),
                ) // insert-if-absent (IGNORE on conflict)
                entries.insert(entry)
                ghostSets.forEach { setEntries.insert(it) }
                touchSession(sessionId)
            }
        }
    }

    override suspend fun addStation(sessionId: String, divisionKey: String, segmentKey: String, raceMode: RaceMode, gender: Gender) {
        val stations = hyroxStations(
            divisionKey = divisionKey,
            raceMode = raceMode,
            gender = gender,
        ).firstOrNull { it.segmentKey == segmentKey } ?: return

        val now = now()
        val blockId = implicitBlockId(sessionId)
        val nextOrder = entries.countForBlock(blockId)
        val entryId = uuid.newId()

        val entry = ExerciseEntryEntity(
            id = entryId,
            blockId = blockId,
            exerciseId = stations.exerciseId,
            orderIndex = nextOrder,
            segmentKey = stations.segmentKey, // tags this entry as a Hyrox station (drives the Standard line + HYROX type)
            createdAt = now,
            updatedAt = now,
        )

        val set = SetEntryEntity(
            id = uuid.newId(),
            exerciseEntryId = entryId,
            setNumber = 1,
            targetReps = stations.targetReps,
            targetLoadKg = stations.targetLoadKg,
            targetDistanceM = stations.targetDistanceM,
            createdAt = now,
            updatedAt = now,
        )

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                blocks.insert(implicitBlock(sessionId, now))
                entries.insert(entry)
                setEntries.insert(set)
                stampDivision(sessionId, divisionKey, overwrite = false, now = now)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun addHyroxVariant(
        sessionId: String,
        divisionKey: String,
        variant: HyroxVariant,
        raceMode: RaceMode,
        gender: Gender,
        replaceExisting: Boolean,
    ) {
        val segments = hyroxFormat(divisionKey, variant, raceMode, gender)
            .filter { it.segmentKey != null }
        if (segments.isEmpty()) return

        val now = now()
        val blockId = implicitBlockId(sessionId)
        // Replacing clears the whole session (every block, not just the implicit one), so the new race
        // starts at order 0; appending continues after what's there. Read once either way → contiguous
        // order, no read-modify-write race.
        val stale = if (replaceExisting) entries.getBySession(sessionId) else emptyList()
        val base = if (replaceExisting) 0 else entries.countForBlock(blockId)

        val rows = segments.mapIndexed { i, seg ->
            val entryId = uuid.newId()
            val entry = ExerciseEntryEntity(
                id = entryId,
                blockId = blockId,
                exerciseId = seg.exerciseId,
                orderIndex = base + i,
                segmentKey = seg.segmentKey,
                createdAt = now,
                updatedAt = now,
            )
            val set = SetEntryEntity(
                id = uuid.newId(),
                exerciseEntryId = entryId,
                setNumber = 1,
                targetReps = seg.targetReps,
                targetLoadKg = seg.targetLoadKg,
                targetDistanceM = seg.targetDistanceM,
                createdAt = now,
                updatedAt = now,
            )
            entry to set
        }

        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                stale.forEach { old ->
                    setEntries.softDeleteForEntry(old.id, now)
                    entries.softDelete(old.id, now)
                }
                blocks.insert(implicitBlock(sessionId, now))
                rows.forEach { (entry, set) ->
                    entries.insert(entry)
                    setEntries.insert(set)
                }
                stampDivision(sessionId, divisionKey, overwrite = replaceExisting, now = now)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun hyroxStations(divisionKey: String, raceMode: RaceMode, gender: Gender): List<HyroxStationModel> = hyroxFormat(
        divisionKey = divisionKey,
        HyroxVariant.FULL,
        raceMode = raceMode,
        gender = gender,
    ).filter { it.stationType == HyroxStationType.STATION }

    override suspend fun stationStandardLabels(divisionKey: String, mode: RaceMode, gender: Gender): Map<String, String> {
        ensureSeeded()
        val format = EventFormat.HYROX
        suspend fun standards(div: String): Map<String, EventSegmentStandardEntity> =
            eventSegmentStandardDao.standardsFor(format, div, mode.name)
                .ifEmpty {
                    eventSegmentStandardDao.standardsFor(
                        format,
                        div,
                        RaceMode.SINGLES.name,
                    )
                }
                .associateBy { it.segmentId }

        val open = standards("${gender.name}_OPEN")
        val pro = standards("${gender.name}_PRO")

        return (open.keys + pro.keys).mapNotNull { segId ->
            standardLabel(pro[segId], open[segId])?.let { segId to it }
        }.toMap()
    }

    private fun standardLabel(pro: EventSegmentStandardEntity?, open: EventSegmentStandardEntity?): String? {
        val reps = open?.targetReps ?: pro?.targetReps
        if (reps != null) {
            val balls = listOfNotNull(
                pro?.loadKg?.let { "${plainKg(it)}kg (Pro)" },
                open?.loadKg?.let { "${plainKg(it)}kg (Open)" },
            )
            return if (balls.isEmpty()) "$reps reps" else "$reps reps · ${balls.joinToString(" / ")} ball"
        }

        val proLoad = pro?.loadKg
        val openLoad = open?.loadKg
        if (proLoad != null || openLoad != null) {
            val tiers = listOfNotNull(
                proLoad?.let { "${plainKg(it)}kg (Pro)" },
                openLoad?.let { "${plainKg(it)}kg (Open)" },
            )
            return tiers.joinToString(" / ")
        }
        val dist = open?.targetDistanceM ?: pro?.targetDistanceM
        return dist?.let { "${it}m" }
    }

    private fun plainKg(kg: Double): String = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()

    override suspend fun updateSessionNotes(sessionId: String, notes: String) {
        val current = sessions.getById(sessionId) ?: return
        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.upsert(
                    current.copy(
                        notes = notes,
                        updatedAt = now,
                        syncStatus = SyncStatus.PENDING,
                    ),
                )
                enqueueOutbox(sessionId, now)
            }
        }
    }

    override suspend fun removeEntry(sessionId: String, entryId: String) {
        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.softDeleteForEntry(entryId, now)
                entries.softDelete(entryId, now)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun addSet(sessionId: String, loggedItemId: String, reps: Int?, loadKg: Double?, timeSec: Int?, distanceM: Int?) {
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
                detectAndStorePrs(sessionId, set.toDomain(), now)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun addTargetSet(sessionId: String, loggedItemId: String, reps: Int?, loadKg: Double?, timeSec: Int?, distanceM: Int?) {
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
                detectAndStorePrs(sessionId, set, now)
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
        formatKey: String? = null,
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
            formatKey = formatKey,
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
        val newBlockEntities = templateBlocks.map { tb ->
            BlockEntity(
                id = newBlockIdByOld.getValue(tb.id),
                sessionId = newSession.id,
                type = tb.type,
                orderIndex = tb.orderIndex,
                rounds = tb.rounds,
                section = tb.section,
                capSeconds = tb.capSeconds,
                workSeconds = tb.workSeconds,
                createdAt = now,
                updatedAt = now,
            )
        }

        val entriesWithSets = entries.getBySession(templateId).map { entry ->
            val newEntryId = uuid.newId()
            val newEntry = ExerciseEntryEntity(
                id = newEntryId,
                blockId = newBlockIdByOld.getValue(entry.blockId),
                exerciseId = entry.exerciseId,
                orderIndex = entry.orderIndex,
                targetSets = entry.targetSets,
                restMs = entry.restMs,
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
                newBlockEntities.forEach { blocks.insert(it) }
                entriesWithSets.forEach { (entry, sets) ->
                    entries.insert(entry)
                    sets.forEach { setEntries.insert(it) }
                }
                enqueueOutbox(newSession.id, now)
            }
        }
        return newSession.toDomain()
    }

    override suspend fun hyroxFormat(divisionKey: String, variant: HyroxVariant, raceMode: RaceMode, gender: Gender): List<HyroxStationModel> {
        ensureSeeded()
        val format = EventFormat.HYROX
        val segments = eventSegmentDao.segmentsForFormat(format)
        val standards = eventSegmentStandardDao.standardsFor(
            formatKey = format,
            divisionKey = divisionKey,
            mode = raceMode.name,
        ).associateBy { it.segmentId }

        val stationEnumByExercise = exercises.getByIds(segments.map { it.exerciseId }.distinct())
            .associate { it.id to it.hyroxStation?.let { name -> HyroxStation.entries.firstOrNull { e -> e.name == name } } }

        val selected = when (variant) {
            HyroxVariant.FIRST_HALF -> segments.filter { it.orderIndex in 1..8 }
            HyroxVariant.SECOND_HALF -> segments.filter { it.orderIndex in 9..16 }
            HyroxVariant.FULL, HyroxVariant.HALVED -> segments
        }

        val halve = variant == HyroxVariant.HALVED

        return selected.mapIndexed { i, seg ->
            segmentStation(i, seg, stationEnumByExercise[seg.exerciseId], standards[seg.id], halve)
        }
    }

    private fun segmentStation(
        index: Int,
        seg: EventSegmentEntity,
        station: HyroxStation?,
        standard: EventSegmentStandardEntity?,
        halve: Boolean,
    ): HyroxStationModel {
        if (seg.kind == SegmentKind.RUN.name) {
            val dist = (seg.distanceM ?: 1000).let { if (halve) it / 2 else it }

            return HyroxStationModel(
                index = index,
                stationType = HyroxStationType.RUN,
                station = null,
                exerciseId = seg.exerciseId,
                title = seg.name,
                detail = seg.descriptor,
                value = "${dist / 1000.0} km",
                targetDistanceM = dist,
                segmentKey = seg.id,
            )
        }

        val stationNumber = seg.orderIndex / 2 // stations sit at even order 2,4,…16 → 1..8
        val title = "$stationNumber. ${seg.name}"
        // Rep-scored station (wall balls): reps + ball weight/target from the division standard.
        if (seg.metric == MetricType.REPS_ONLY.name) {
            val reps = (standard?.targetReps ?: seg.reps ?: 100).let { if (halve) it / 2 else it }

            return HyroxStationModel(
                index = index,
                stationType = HyroxStationType.STATION,
                station = station,
                exerciseId = seg.exerciseId,
                title = title,
                detail = "${standard?.loadKg?.toInt() ?: 0}kg Ball • ${standard?.targetHeightM ?: "-"}m Target",
                value = "$reps reps",
                targetReps = reps,
                targetLoadKg = standard?.loadKg,
                loadDisplay = standard?.loadDisplay,
                stationNumber = stationNumber,
                segmentKey = seg.id,
            )
        }

        val dist = (seg.distanceM ?: 0).let { if (halve) it / 2 else it }
        return if (standard?.loadKg != null) {
            HyroxStationModel(
                index = index,
                stationType = HyroxStationType.STATION,
                station = station,
                exerciseId = seg.exerciseId,
                title = title,
                detail = "${dist}m ${seg.descriptor}",
                value = standard.loadDisplay ?: "",
                targetDistanceM = dist,
                targetLoadKg = standard.loadKg,
                loadDisplay = standard.loadDisplay,
                stationNumber = stationNumber,
                segmentKey = seg.id,
            )
        } else {
            HyroxStationModel(
                index = index,
                stationType = HyroxStationType.STATION,
                station = station,
                exerciseId = seg.exerciseId,
                title = title,
                detail = "${dist}m ${seg.descriptor}",
                value = "${dist}m",
                targetDistanceM = dist,
                stationNumber = stationNumber,
                segmentKey = seg.id,
            )
        }
    }

    override suspend fun startHyroxSession(
        divisionKey: String,
        variant: HyroxVariant,
        raceMode: RaceMode,
        gender: Gender,
        templateId: String?,
    ): String {
        val session = insertSession(
            name = displayName(SessionType.HYROX),
            type = SessionType.HYROX,
            source = SessionSource.RACE_SIM,
            templateId = templateId,
            formatKey = EventFormat.HYROX,
        )
        // Reuses the one segment seeder (see the interface KDoc): contiguous orderIndex from 0 — the
        // contract recordHyroxSplit resolves splits by — plus division stamping and the outbox row.
        addHyroxVariant(session.id, divisionKey, variant, raceMode, gender)
        return session.id
    }

    override suspend fun recordHyroxSplit(sessionId: String, stepIndex: Int, elapsedSec: Int) {
        val entry =
            entries.getBySession(sessionId).firstOrNull { it.orderIndex == stepIndex } ?: return
        val set = setEntries.getForEntry(entry.id).firstOrNull() ?: return
        val now = now()
        // Promote the segment's targets to actuals alongside the split. `reps` is load-bearing, not
        // tidiness: Wall Balls is the only REPS_TIME station and detectPrs's REPS_TIME branch needs a
        // rep count, so without it that one station would silently never record a PB while the other
        // seven did. `loadKg` is display parity with the manual path (no station is weight-scored).
        val updated = set.copy(
            timeSec = elapsedSec,
            distanceM = set.targetDistanceM ?: set.distanceM, // record the full distance for pace
            reps = set.targetReps ?: set.reps,
            loadKg = set.targetLoadKg ?: set.loadKg,
            updatedAt = now,
        )
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                setEntries.update(updated)
                // Same as addSet/updateSet — without this a timed race logged splits but produced no
                // records at all, so nothing from the live timer ever reached the Station board.
                detectAndStorePrs(sessionId, updated.toDomain(), now)
                touchSession(sessionId)
            }
        }
    }

    override suspend fun finishSession(sessionId: String, derivedType: DomainSessionType?) {
        val current = sessions.getById(sessionId) ?: return
        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.upsert(
                    current.copy(
                        finishedAt = now,
                        type = derivedType?.name ?: current.type,
                        // Keep the display name in step with the auto-derived type (the FAB creates the
                        // session with a placeholder type/name before anything is logged).
                        name = derivedType?.let { displayName(it.name) } ?: current.name,
                        updatedAt = now,
                        syncStatus = SyncStatus.PENDING,
                    ),
                )
                enqueueOutbox(sessionId, now)
            }
        }
    }

    override suspend fun discardSessionIfEmpty(sessionId: String): Boolean {
        val current = sessions.getById(sessionId) ?: return false
        // Idempotent + narrow: a live, real session with nothing logged. Notes are deliberately NOT
        // content — a note against no entries is still not a training session — so they don't stay it.
        if (current.deletedAt != null || current.isTemplate) return false
        if (entries.getBySession(sessionId).isNotEmpty()) return false

        val now = now()
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                sessions.upsert(
                    current.copy(deletedAt = now, updatedAt = now, syncStatus = SyncStatus.PENDING),
                )
                enqueueOutbox(sessionId, now)
            }
        }
        return true
    }

    /**
     * Records the athlete's division on the session that Hyrox content is being added to.
     *
     * [detectAndStorePrs] reads `sessions.divisionKey` to bucket a PB, so while this was never
     * written every record was stored division-less and the division bucketing in [detectPrs] —
     * "Sled Push @ Men Pro" vs "@ Women Open" — could never take effect.
     *
     * First write wins: the division is fixed by the first Hyrox content to land in the session, so
     * switching division later can't re-bucket PBs for sets already logged under the old one. The
     * exception is [overwrite], passed when the session's contents are being wholly replaced and
     * there is therefore nothing left to keep consistent with.
     */
    private suspend fun stampDivision(sessionId: String, divisionKey: String, overwrite: Boolean, now: Long) {
        val current = sessions.getById(sessionId) ?: return
        if (current.divisionKey == divisionKey) return
        if (!overwrite && current.divisionKey != null) return
        sessions.upsert(
            current.copy(divisionKey = divisionKey, updatedAt = now, syncStatus = SyncStatus.PENDING),
        )
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
                        ExerciseEntryEntity(
                            id = entryId,
                            blockId = implicitBlockId(sessionId),
                            exerciseId = "bench-press",
                            orderIndex = 0,
                            createdAt = ts,
                            updatedAt = ts,
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

    override suspend fun ensureSeeded() {
        // Versioned re-seed: fresh installations import; existing installs whose catalog predates the
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
                        val entryIds = entries.getBySession(t.sessionEntity.id).map { it.id }
                        if (entryIds.isNotEmpty()) setEntries.deleteForEntries(entryIds)
                        entries.deleteBySession(t.sessionEntity.id) // before blocks (its subquery joins blocks)
                        blocks.deleteBySession(t.sessionEntity.id)
                        sessions.upsert(t.sessionEntity)
                        t.blockEntities.forEach { blocks.insert(it) }
                        t.entries.forEach { entries.insert(it) }
                        t.sets.forEach { setEntries.insert(it) }
                    }
                }
            }
            syncMeta.set(
                SyncMeta(
                    SyncMetaKeys.TEMPLATE_SEED_VERSION,
                    TEMPLATE_SEED_VERSION.toString(),
                ),
            )
        }

        // Event-format reference tables (formats / segments / divisions / per-division standards).
        // Reference data, like the catalog — no outbox. Fixed slug ids → idempotent upsert. Adding a
        // new event (DEKA/CrossFit) is a seed change here + a VERSION bump, never a migration.
        val eventSeeded = syncMeta.get(SyncMetaKeys.EVENT_SEED_VERSION)?.toIntOrNull() ?: 0
        if (eventSeeded < EventSeed.VERSION) {
            eventFormatDao.upsertFormats(EventSeed.formats)
            eventSegmentDao.upsertSegments(EventSeed.segments)
            eventDivisionDao.upsertDivisions(EventSeed.divisions)
            eventSegmentStandardDao.upsertStandards(EventSeed.standards)
            syncMeta.set(SyncMeta(SyncMetaKeys.EVENT_SEED_VERSION, EventSeed.VERSION.toString()))
        }
    }
}
