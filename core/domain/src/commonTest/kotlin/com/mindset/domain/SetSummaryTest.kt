@file:OptIn(ExperimentalTime::class)

package com.mindset.domain

import com.mindset.model.CaptureFields
import com.mindset.model.SetEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val T0 = Instant.fromEpochMilliseconds(0)

private fun entry(
    reps: Int? = null,
    loadKg: Double? = null,
    timeSec: Int? = null,
    distanceM: Int? = null,
    calories: Int? = null,
) = SetEntry(
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
    createdAt = T0,
    updatedAt = T0,
    deletedAt = null,
)

class SetSummaryTest {
    @Test
    fun weightReps_formatsRepsTimesLoad() {
        assertEquals("5 × 100 kg", entry(reps = 5, loadKg = 100.0).detailSummary(CaptureFields.WeightReps, WeightUnit.KG))
    }

    @Test
    fun distanceTime_formatsMetresAndClock() {
        assertEquals("1000 m · 4:32", entry(distanceM = 1000, timeSec = 272).detailSummary(CaptureFields.DistanceTime, WeightUnit.KG))
    }

    @Test
    fun repsTime_formatsRepsAndClock() {
        assertEquals("100 reps · 3:15", entry(reps = 100, timeSec = 195).detailSummary(CaptureFields.RepsTime, WeightUnit.KG))
    }

    @Test
    fun duration_formatsClock() {
        assertEquals("0:45", entry(timeSec = 45).detailSummary(CaptureFields.Duration, WeightUnit.KG))
    }

    @Test
    fun calories_formats() {
        assertEquals("80 cal", entry(calories = 80).detailSummary(CaptureFields.Calories, WeightUnit.KG))
    }

    @Test
    fun missingActuals_renderEmDash_notZero() {
        // A quick-added-but-never-logged station: no actual time/distance → dashes, never "0".
        assertEquals("— · —", entry().detailSummary(CaptureFields.DistanceTime, WeightUnit.KG))
        assertEquals("— × —", entry().detailSummary(CaptureFields.WeightReps, WeightUnit.KG))
    }

    @Test
    fun weightReps_convertsToDisplayUnit() {
        assertEquals("220.4 lb", entry(reps = 1, loadKg = 100.0).detailSummary(CaptureFields.WeightReps, WeightUnit.LB).substringAfter("× "))
    }
}
