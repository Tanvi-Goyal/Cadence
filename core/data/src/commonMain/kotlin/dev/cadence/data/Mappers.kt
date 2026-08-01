@file:OptIn(ExperimentalTime::class)

package dev.cadence.data

import dev.cadence.data.local.Block as BlockEntity
import dev.cadence.data.local.Exercise as ExerciseEntity
import dev.cadence.data.local.ExerciseEntry as ExerciseEntryEntity
import dev.cadence.data.local.PersonalRecord as PersonalRecordEntity
import dev.cadence.data.local.PlannedSession as PlannedSessionEntity
import dev.cadence.data.local.Session as SessionEntity
import dev.cadence.data.local.SetEntry as SetEntryEntity
import dev.cadence.model.Block
import dev.cadence.model.BlockDetail
import dev.cadence.model.BlockSection
import dev.cadence.model.BlockType
import dev.cadence.model.ConditioningFormat
import dev.cadence.model.Exercise
import dev.cadence.model.ExerciseEntry
import dev.cadence.model.ExerciseEntryDetail
import dev.cadence.model.HyroxStation
import dev.cadence.model.MetricType
import dev.cadence.model.Modality
import dev.cadence.model.PersonalRecord
import dev.cadence.model.PlannedSession
import dev.cadence.model.PrKind
import dev.cadence.model.Session
import dev.cadence.model.SessionDetail
import dev.cadence.model.SessionSource
import dev.cadence.model.SessionType
import dev.cadence.model.SetEntry
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Entity ↔ domain mapping — the ONLY place both worlds meet. Room entities (epoch-millis Longs,
 * string-const enums, JSON list columns) become pure domain types (Instant, real enums, real lists)
 * and back. Keeping this seam in one file is what lets the domain/UI stay ignorant of Room.
 *
 * Interim note (through A6): `Exercise.modality`/`defaultMetric` are null until A7's re-seed, so the
 * legacy fallbacks derive them from the old `metric`/`category` columns.
 */

private inline fun <reified T : Enum<T>> safeEnum(name: String): T? =
    enumValues<T>().firstOrNull { it.name == name }

private fun legacyModality(category: String?): Modality = when (category?.lowercase()) {
    "cardio" -> Modality.CONDITIONING
    "stretching" -> Modality.MOBILITY
    "plyometrics" -> Modality.CONDITIONING
    else -> Modality.STRENGTH
}

private fun legacyMetric(metric: String): MetricType =
    if (metric == "TIME_DISTANCE") MetricType.DISTANCE_TIME else MetricType.WEIGHT_REPS

private fun ms(value: Long): Instant = Instant.fromEpochMilliseconds(value)

/** The deterministic id of a session's (currently sole, implicit) STRAIGHT block. */
internal fun implicitBlockId(sessionId: String): String = "block-$sessionId"

internal fun SessionEntity.toDomain(): Session = Session(
    id = id,
    startedAt = ms(startedAt),
    name = name,
    type = safeEnum<SessionType>(type) ?: SessionType.STRENGTH,
    notes = notes,
    isTemplate = isTemplate,
    source = safeEnum<SessionSource>(source) ?: SessionSource.MANUAL,
    templateId = templateId,
    category = category,
    focus = focus,
    programWeek = programWeek,
    finishedAt = finishedAt?.let(::ms),
    createdAt = ms(if (createdAt != 0L) createdAt else startedAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)

internal fun BlockEntity.toDomain(): Block = Block(
    id = id,
    sessionId = sessionId,
    type = safeEnum<BlockType>(type) ?: BlockType.STRAIGHT,
    orderIndex = orderIndex,
    rounds = rounds,
    restBetweenRoundsMs = restBetweenRoundsMs,
    label = label,
    section = section?.let { safeEnum<BlockSection>(it) },
    conditioningFormat = conditioningFormat?.let { safeEnum<ConditioningFormat>(it) },
    capSeconds = capSeconds,
    workSeconds = workSeconds,
    createdAt = ms(createdAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)

internal fun ExerciseEntryEntity.toDomain(): ExerciseEntry = ExerciseEntry(
    id = id,
    blockId = blockId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    targetSets = targetSets,
    restMs = restMs,
    note = note,
    eachSide = eachSide ?: false,
    createdAt = ms(createdAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)

internal fun SetEntryEntity.toDomain(): SetEntry = SetEntry(
    id = id,
    exerciseEntryId = exerciseEntryId,
    setNumber = setNumber,
    reps = reps,
    loadKg = loadKg,
    timeSec = timeSec,
    distanceM = distanceM,
    calories = calories,
    rpe = rpe,
    targetReps = targetReps,
    targetLoadKg = targetLoadKg,
    targetTimeSec = targetTimeSec,
    targetDistanceM = targetDistanceM,
    targetCalories = targetCalories,
    createdAt = ms(createdAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)

/** Domain → entity for persistence (used by `updateSet`). */
internal fun SetEntry.toEntity(): SetEntryEntity = SetEntryEntity(
    id = id,
    exerciseEntryId = exerciseEntryId,
    setNumber = setNumber,
    reps = reps,
    loadKg = loadKg,
    timeSec = timeSec,
    distanceM = distanceM,
    rpe = rpe,
    targetReps = targetReps,
    targetLoadKg = targetLoadKg,
    targetTimeSec = targetTimeSec,
    targetDistanceM = targetDistanceM,
    calories = calories,
    targetCalories = targetCalories,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
    deletedAt = deletedAt?.toEpochMilliseconds(),
)

internal fun PlannedSessionEntity.toDomain(): PlannedSession = PlannedSession(
    id = id,
    name = name,
    type = safeEnum<SessionType>(type) ?: SessionType.STRENGTH,
    targetDurationMin = targetDurationMin,
    focus = focus,
)

internal fun PersonalRecordEntity.toDomain(): PersonalRecord = PersonalRecord(
    id = id,
    exerciseId = exerciseId,
    kind = safeEnum<PrKind>(kind) ?: PrKind.MAX_WEIGHT,
    value = value,
    distanceBucketM = distanceBucketM,
    achievedAt = ms(achievedAt),
    sourceSetId = sourceSetId,
    createdAt = ms(createdAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)

internal fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    modality = modality?.let { safeEnum<Modality>(it) } ?: legacyModality(category),
    defaultMetric = defaultMetric?.let { safeEnum<MetricType>(it) } ?: legacyMetric(metric),
    hyroxStation = hyroxStation?.let { safeEnum<HyroxStation>(it) },
    category = category,
    force = force,
    level = level,
    mechanic = mechanic,
    equipment = equipment,
    primaryMuscles = primaryMusclesList,
    secondaryMuscles = secondaryMusclesList,
    instructions = instructionsList,
    imageUrls = imageUrlsList,
)

private fun fallbackExercise(id: String): Exercise = Exercise(
    id = id, name = id, modality = Modality.STRENGTH, defaultMetric = MetricType.WEIGHT_REPS,
    hyroxStation = null, category = null, force = null, level = null, mechanic = null,
    equipment = null, primaryMuscles = emptyList(), secondaryMuscles = emptyList(),
    instructions = emptyList(), imageUrls = emptyList(),
)

/**
 * Assemble the hydrated [SessionDetail] the UI renders, from the real block graph:
 * blocks → their exercise entries (each with resolved catalog [Exercise] + derived capture fields)
 * → their sets. [exercises] must resolve every `exerciseId` the session references.
 */
internal fun buildSessionDetail(
    session: SessionEntity,
    blocks: List<BlockEntity>,
    entries: List<ExerciseEntryEntity>,
    sets: List<SetEntryEntity>,
    exercises: Map<String, Exercise>,
): SessionDetail {
    val setsByEntry = sets.groupBy { it.exerciseEntryId }
    val entriesByBlock = entries.groupBy { it.blockId }
    val blockDetails = blocks.sortedBy { it.orderIndex }.map { block ->
        val entryDetails =
            entriesByBlock[block.id].orEmpty().sortedBy { it.orderIndex }.map { entry ->
                ExerciseEntryDetail(
                    entry = entry.toDomain(),
                    exercise = exercises[entry.exerciseId] ?: fallbackExercise(entry.exerciseId),
                    sets = setsByEntry[entry.id].orEmpty().sortedBy { it.setNumber }
                        .map { it.toDomain() },
                )
            }
        BlockDetail(block.toDomain(), entryDetails)
    }
    return SessionDetail(session = session.toDomain(), blocks = blockDetails)
}
