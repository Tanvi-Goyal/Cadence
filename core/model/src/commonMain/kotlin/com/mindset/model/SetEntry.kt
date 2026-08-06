@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * One set — the single unit of work — under an [ExerciseEntry]. Every set holds BOTH a prescription
 * and a performance so one polymorphic row serves all modalities:
 * - **Actuals** ([reps]/[loadKg]/[timeSec]/[distanceM]/[calories]) — what was performed.
 * - **Targets** ([targetReps]/…/[targetCalories]) — the prescription; in a template row only the
 *   targets are set. Which pair is meaningful is decided by the exercise's [Exercise.defaultMetric].
 *
 * v8 additions over the pre-v8 row: [calories]/[targetCalories] (for erg/Hyrox capture) and the
 * [Syncable] envelope; the parent key is [exerciseEntryId] (was `loggedItemId`).
 */
data class SetEntry(
    override val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    // Performance (actuals):
    val reps: Int?,
    val loadKg: Double?,
    val timeSec: Int?,
    val distanceM: Int?,
    val calories: Int?,
    val rpe: Int?,
    // Prescription (targets):
    val targetReps: Int?,
    val targetLoadKg: Double?,
    val targetTimeSec: Int?,
    val targetDistanceM: Int?,
    val targetCalories: Int?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
