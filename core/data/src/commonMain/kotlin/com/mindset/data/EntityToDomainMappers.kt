@file:OptIn(ExperimentalTime::class)

package com.mindset.data

import com.mindset.model.Block
import com.mindset.model.BlockDetail
import com.mindset.model.BlockSection
import com.mindset.model.BlockType
import com.mindset.model.Exercise
import com.mindset.model.ExerciseEntry
import com.mindset.model.ExerciseEntryDetail
import com.mindset.model.HyroxStation
import com.mindset.model.MetricType
import com.mindset.model.MetricType.WEIGHT_REPS
import com.mindset.model.Modality
import com.mindset.model.PersonalRecord
import com.mindset.model.PlannedSession
import com.mindset.model.PrKind
import com.mindset.model.Session
import com.mindset.model.SessionDetail
import com.mindset.model.SessionSource
import com.mindset.model.SessionType
import com.mindset.model.SetEntry
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import com.mindset.data.local.BlockEntity as BlockEntity
import com.mindset.data.local.Exercise as ExerciseEntity
import com.mindset.data.local.ExerciseEntryEntity as ExerciseEntryEntity
import com.mindset.data.local.PersonalRecord as PersonalRecordEntity
import com.mindset.data.local.PlannedSession as PlannedSessionEntity
import com.mindset.data.local.SessionEntity as SessionEntity
import com.mindset.data.local.SetEntryEntity as SetEntryEntity

/**
 * Entity ↔ domain mapping — the ONLY place both worlds meet. Room entities (epoch-millis Longs,
 * string-const enums, JSON list columns) become pure domain types (Instant, real enums, real lists)
 * and back. Keeping this seam in one file is what lets the domain/UI stay ignorant of Room.
 *
 * Interim note (through A6): `Exercise.modality`/`defaultMetric` are null until A7's re-seed, so the
 * legacy fallbacks derive them from the old `metric`/`category` columns.
 */

private inline fun <reified T : Enum<T>> safeEnum(name: String): T? = enumValues<T>().firstOrNull { it.name == name }

private fun ms(value: Long): Instant = Instant.fromEpochMilliseconds(value)

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
    raceGoalId = raceGoalId,
    formatKey = formatKey,
    divisionKey = divisionKey,
    avgHeartRate = avgHeartRate,
    caloriesKcal = caloriesKcal,
    perceivedEffort = perceivedEffort,
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
    section = section?.let { safeEnum<BlockSection>(it) },
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
    eachSide = eachSide ?: false,
    segmentKey = segmentKey,
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

internal fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    modality = modality?.let { safeEnum<Modality>(it) } ?: Modality.STRENGTH,
    defaultMetric = defaultMetric?.let { safeEnum<MetricType>(it) } ?: WEIGHT_REPS,
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
    divisionKey = divisionKey,
    achievedAt = ms(achievedAt),
    sourceSetId = sourceSetId,
    createdAt = ms(createdAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)
