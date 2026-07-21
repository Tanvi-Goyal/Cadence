@file:OptIn(ExperimentalTime::class)

package dev.cadence.data

import dev.cadence.data.local.Exercise as ExerciseEntity
import dev.cadence.data.local.LoggedItem
import dev.cadence.data.local.Session as SessionEntity
import dev.cadence.data.local.SetEntry as SetEntryEntity
import dev.cadence.model.Block
import dev.cadence.model.BlockDetail
import dev.cadence.model.BlockType
import dev.cadence.model.Exercise
import dev.cadence.model.ExerciseEntry
import dev.cadence.model.ExerciseEntryDetail
import dev.cadence.model.HyroxStation
import dev.cadence.model.MetricType
import dev.cadence.model.Modality
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
 * Interim note (A4b): the DB is still the pre-v8 3-level tree (session → logged_item → set), so
 * [buildSessionDetail] synthesizes a single implicit STRAIGHT [Block] per session. A4c replaces the
 * synthesis with real persisted blocks; the block id it mints here (`block-<sessionId>`) matches the
 * A6 migration's convention so nothing downstream shifts. Likewise `Exercise.modality`/`defaultMetric`
 * are null until A7's re-seed, so the legacy fallbacks below derive them from the old columns.
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

internal fun SessionEntity.toDomain(): Session = Session(
    id = id,
    startedAt = ms(startedAt),
    name = name,
    type = safeEnum<SessionType>(type) ?: SessionType.STRENGTH,
    notes = notes,
    isTemplate = isTemplate,
    source = safeEnum<SessionSource>(source) ?: SessionSource.MANUAL,
    templateId = templateId,
    createdAt = ms(if (createdAt != 0L) createdAt else startedAt),
    updatedAt = ms(updatedAt),
    deletedAt = deletedAt?.let(::ms),
)

internal fun SetEntryEntity.toDomain(): SetEntry = SetEntry(
    id = id,
    exerciseEntryId = loggedItemId,
    setNumber = setNumber,
    reps = reps, loadKg = loadKg, timeSec = timeSec, distanceM = distanceM,
    calories = calories, rpe = rpe,
    targetReps = targetReps, targetLoadKg = targetLoadKg, targetTimeSec = targetTimeSec,
    targetDistanceM = targetDistanceM, targetCalories = targetCalories,
    createdAt = ms(createdAt), updatedAt = ms(updatedAt), deletedAt = deletedAt?.let(::ms),
)

/** Domain → entity for persistence (used by `updateSet`). Maps `exerciseEntryId` → `loggedItemId`. */
internal fun SetEntry.toEntity(): SetEntryEntity = SetEntryEntity(
    id = id,
    loggedItemId = exerciseEntryId,
    setNumber = setNumber,
    reps = reps, loadKg = loadKg, timeSec = timeSec, distanceM = distanceM, rpe = rpe,
    targetReps = targetReps, targetLoadKg = targetLoadKg, targetTimeSec = targetTimeSec,
    targetDistanceM = targetDistanceM,
    calories = calories, targetCalories = targetCalories,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
    deletedAt = deletedAt?.toEpochMilliseconds(),
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

/** The deterministic id of a session's implicit block — same convention the A6 migration will use. */
internal fun implicitBlockId(sessionId: String): String = "block-$sessionId"

private fun fallbackExercise(id: String): Exercise = Exercise(
    id = id, name = id, modality = Modality.STRENGTH, defaultMetric = MetricType.WEIGHT_REPS,
    hyroxStation = null, category = null, force = null, level = null, mechanic = null,
    equipment = null, primaryMuscles = emptyList(), secondaryMuscles = emptyList(),
    instructions = emptyList(), imageUrls = emptyList(),
)

/**
 * Assemble the hydrated [SessionDetail] the UI renders. Until A4c persists real blocks, all entries
 * live in one synthesized STRAIGHT block. [exercises] must already resolve every `exerciseId` the
 * session references (see `SessionRepositoryImpl.observeSessionDetail`).
 */
internal fun buildSessionDetail(
    session: SessionEntity,
    items: List<LoggedItem>,
    sets: List<SetEntryEntity>,
    exercises: Map<String, Exercise>,
): SessionDetail {
    val setsByItem = sets.groupBy { it.loggedItemId }
    val blockId = implicitBlockId(session.id)
    val createdAt = ms(if (session.createdAt != 0L) session.createdAt else session.startedAt)
    val updatedAt = ms(session.updatedAt)

    val entries = items.sortedBy { it.orderIndex }.map { item ->
        ExerciseEntryDetail(
            entry = ExerciseEntry(
                id = item.id,
                blockId = blockId,
                exerciseId = item.exerciseId,
                orderIndex = item.orderIndex,
                targetSets = null,
                restMs = null,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = null,
            ),
            exercise = exercises[item.exerciseId] ?: fallbackExercise(item.exerciseId),
            sets = setsByItem[item.id].orEmpty().sortedBy { it.setNumber }.map { it.toDomain() },
        )
    }

    val block = Block(
        id = blockId, sessionId = session.id, type = BlockType.STRAIGHT, orderIndex = 0,
        rounds = 1, restBetweenRoundsMs = null, label = null,
        createdAt = createdAt, updatedAt = updatedAt, deletedAt = null,
    )
    return SessionDetail(session = session.toDomain(), blocks = listOf(BlockDetail(block, entries)))
}
