@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * One set — the single unit of work — under an [ExerciseEntry]
 */
data class SetEntry(
    override val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    val reps: Int?,
    val loadKg: Double?,
    val timeSec: Int?,
    val distanceM: Int?,
    val calories: Int?,
    val rpe: Int?,
    val targetReps: Int?,
    val targetLoadKg: Double?,
    val targetTimeSec: Int?,
    val targetDistanceM: Int?,
    val targetCalories: Int?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
