@file:OptIn(ExperimentalTime::class)

package com.mindset.domain

import com.mindset.model.Exercise
import com.mindset.model.MetricType
import com.mindset.model.Modality
import com.mindset.model.PersonalRecord
import com.mindset.model.PrKind
import com.mindset.model.SetEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val EPOCH = Instant.fromEpochMilliseconds(0)

private fun exercise(metric: MetricType) = Exercise(
    id = "x",
    name = "X",
    modality = Modality.STRENGTH,
    defaultMetric = metric,
    hyroxStation = null,
    category = null,
    force = null,
    level = null,
    mechanic = null,
    equipment = null,
    primaryMuscles = emptyList(),
    secondaryMuscles = emptyList(),
    instructions = emptyList(),
    imageUrls = emptyList(),
)

private fun set(reps: Int? = null, loadKg: Double? = null, timeSec: Int? = null, distanceM: Int? = null, calories: Int? = null) = SetEntry(
    id = "s",
    exerciseEntryId = "e",
    setNumber = 1,
    reps = reps,
    loadKg = loadKg,
    timeSec = timeSec,
    distanceM = distanceM,
    calories = calories,
    rpe = null,
    targetReps = null,
    targetLoadKg = null,
    targetTimeSec = null,
    targetDistanceM = null,
    targetCalories = null,
    createdAt = EPOCH,
    updatedAt = EPOCH,
    deletedAt = null,
)

private fun pr(kind: PrKind, value: Double, bucket: Int? = null) = PersonalRecord(
    id = "pr-${kind.name}-$bucket",
    exerciseId = "x",
    kind = kind,
    value = value,
    distanceBucketM = bucket,
    achievedAt = EPOCH,
    sourceSetId = "s0",
    createdAt = EPOCH,
    updatedAt = EPOCH,
    deletedAt = null,
)

class PbDetectionTest {
    @Test
    fun epley_formula_is_weight_times_one_plus_reps_over_thirty() {
        assertEquals(100.0 * (1 + 5 / 30.0), epleyOneRepMax(100.0, 5))
        assertEquals(60.0, epleyOneRepMax(60.0, 0), "0 reps ⇒ the lift itself")
    }

    @Test
    fun first_weight_reps_set_records_both_e1rm_and_max_weight() {
        val result =
            detectPrs(exercise(MetricType.WEIGHT_REPS), set(reps = 5, loadKg = 100.0), emptyList())
        assertEquals(setOf(PrKind.EST_1RM, PrKind.MAX_WEIGHT), result.map { it.kind }.toSet())
        assertEquals(epleyOneRepMax(100.0, 5), result.first { it.kind == PrKind.EST_1RM }.value)
        assertEquals(100.0, result.first { it.kind == PrKind.MAX_WEIGHT }.value)
    }

    @Test
    fun equal_e1rm_and_weight_are_not_new_prs() {
        val current =
            listOf(pr(PrKind.EST_1RM, epleyOneRepMax(100.0, 5)), pr(PrKind.MAX_WEIGHT, 100.0))
        // 3×115 e1rm ties the standing 5×100 e1rm, and 100 kg ties max weight → nothing new.
        val tyingE1rm = set(reps = 5, loadKg = 100.0)
        assertTrue(detectPrs(exercise(MetricType.WEIGHT_REPS), tyingE1rm, current).isEmpty())
    }

    @Test
    fun heavier_or_stronger_beats_the_standing_record() {
        val current =
            listOf(pr(PrKind.EST_1RM, epleyOneRepMax(100.0, 5)), pr(PrKind.MAX_WEIGHT, 100.0))
        val result =
            detectPrs(exercise(MetricType.WEIGHT_REPS), set(reps = 3, loadKg = 105.0), current)
        // 105 kg > 100 kg (max weight); e1rm 105×(1+3/30)=115.5 > 116.67? no → only MAX_WEIGHT here.
        assertEquals(listOf(PrKind.MAX_WEIGHT), result.map { it.kind })
        assertEquals(105.0, result.first().value)
    }

    @Test
    fun reps_only_tracks_max_reps_strictly() {
        val ex = exercise(MetricType.REPS_ONLY)
        assertEquals(
            listOf(PrKind.MAX_REPS),
            detectPrs(ex, set(reps = 20), emptyList()).map { it.kind },
        )
        assertTrue(
            detectPrs(ex, set(reps = 15), listOf(pr(PrKind.MAX_REPS, 15.0))).isEmpty(),
            "tie is not a PR",
        )
        assertEquals(
            21.0,
            detectPrs(ex, set(reps = 21), listOf(pr(PrKind.MAX_REPS, 20.0))).first().value,
        )
    }

    @Test
    fun distance_time_records_best_time_per_bucket_lower_wins() {
        val ex = exercise(MetricType.DISTANCE_TIME)
        val first = detectPrs(ex, set(distanceM = 1000, timeSec = 240), emptyList())
        assertEquals(1000, first.first().distanceBucketM)
        assertEquals(240.0, first.first().value)

        val current = listOf(pr(PrKind.BEST_TIME, 240.0, bucket = 1000))
        assertTrue(
            detectPrs(ex, set(distanceM = 1000, timeSec = 240), current).isEmpty(),
            "equal time not a PR",
        )
        assertEquals(
            230.0,
            detectPrs(ex, set(distanceM = 1000, timeSec = 230), current).first().value,
        )
        // A different distance is a different bucket → still a PR against a 1000 m record.
        assertEquals(
            2000,
            detectPrs(ex, set(distanceM = 2000, timeSec = 500), current).first().distanceBucketM,
        )
    }

    @Test
    fun calories_tracks_max_calories() {
        val ex = exercise(MetricType.CALORIES)
        assertEquals(80.0, detectPrs(ex, set(calories = 80), emptyList()).first().value)
        assertTrue(
            detectPrs(ex, set(calories = 70), listOf(pr(PrKind.MAX_CALORIES, 70.0))).isEmpty(),
        )
    }

    @Test
    fun duration_has_no_default_record() {
        assertTrue(
            detectPrs(exercise(MetricType.DURATION), set(timeSec = 120), emptyList()).isEmpty(),
        )
    }

    @Test
    fun incomplete_actuals_yield_no_record() {
        // WEIGHT_REPS with a missing load can't produce a lift PR.
        assertTrue(
            detectPrs(exercise(MetricType.WEIGHT_REPS), set(reps = 5), emptyList()).isEmpty(),
        )
    }
}
