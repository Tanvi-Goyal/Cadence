@file:OptIn(ExperimentalTime::class)

package dev.cadence.domain

import dev.cadence.model.Block
import dev.cadence.model.BlockDetail
import dev.cadence.model.BlockType
import dev.cadence.model.Exercise
import dev.cadence.model.ExerciseEntry
import dev.cadence.model.ExerciseEntryDetail
import dev.cadence.model.MetricType
import dev.cadence.model.Modality
import dev.cadence.model.Session
import dev.cadence.model.SessionDetail
import dev.cadence.model.SessionSource
import dev.cadence.model.SessionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val EPOCH = Instant.fromEpochMilliseconds(0)

private fun exercise(metric: MetricType) = Exercise(
    id = "x-${metric.name}", name = metric.name, modality = Modality.STRENGTH, defaultMetric = metric,
    hyroxStation = null, category = null, force = null, level = null, mechanic = null, equipment = null,
    primaryMuscles = emptyList(), secondaryMuscles = emptyList(), instructions = emptyList(), imageUrls = emptyList(),
)

private fun entryDetail(metric: MetricType, segmentKey: String? = null): ExerciseEntryDetail =
    ExerciseEntryDetail(
        entry = ExerciseEntry(
            id = "e-${metric.name}", blockId = "b", exerciseId = "x-${metric.name}", orderIndex = 0,
            targetSets = null, restMs = null, segmentKey = segmentKey,
            createdAt = EPOCH, updatedAt = EPOCH, deletedAt = null,
        ),
        exercise = exercise(metric),
        sets = emptyList(),
    )

private fun detail(vararg entries: ExerciseEntryDetail): SessionDetail = SessionDetail(
    session = Session(
        id = "s", startedAt = EPOCH, name = "S", type = SessionType.STRENGTH, notes = null,
        isTemplate = false, source = SessionSource.MANUAL, templateId = null,
        createdAt = EPOCH, updatedAt = EPOCH, deletedAt = null,
    ),
    blocks = listOf(
        BlockDetail(
            block = Block(
                id = "b", sessionId = "s", type = BlockType.STRAIGHT, orderIndex = 0,
                rounds = 1, restBetweenRoundsMs = null, label = null,
                createdAt = EPOCH, updatedAt = EPOCH, deletedAt = null,
            ),
            entries = entries.toList(),
        ),
    ),
)

class SessionTypeDerivationTest {

    @Test
    fun empty_session_defaults_to_strength() {
        assertEquals(SessionType.STRENGTH, deriveSessionType(null))
        assertEquals(SessionType.STRENGTH, deriveSessionType(detail()))
    }

    @Test
    fun all_strength_exercises_derive_strength() {
        assertEquals(
            SessionType.STRENGTH,
            deriveSessionType(detail(entryDetail(MetricType.WEIGHT_REPS), entryDetail(MetricType.REPS_ONLY))),
        )
    }

    @Test
    fun all_conditioning_exercises_derive_conditioning() {
        assertEquals(
            SessionType.CONDITIONING,
            deriveSessionType(detail(entryDetail(MetricType.DISTANCE_TIME), entryDetail(MetricType.DURATION))),
        )
    }

    @Test
    fun strength_plus_conditioning_derives_mixed() {
        assertEquals(
            SessionType.MIXED,
            deriveSessionType(detail(entryDetail(MetricType.WEIGHT_REPS), entryDetail(MetricType.DISTANCE_TIME))),
        )
    }

    @Test
    fun any_station_entry_derives_hyrox() {
        // A segmentKey wins even alongside strength exercises.
        assertEquals(
            SessionType.HYROX,
            deriveSessionType(
                detail(
                    entryDetail(MetricType.WEIGHT_REPS),
                    entryDetail(MetricType.DISTANCE_TIME, segmentKey = "hyrox:04-sled-push"),
                ),
            ),
        )
    }
}
