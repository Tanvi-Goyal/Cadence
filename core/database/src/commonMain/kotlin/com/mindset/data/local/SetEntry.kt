package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * One set — the single unit of work — under an [ExerciseEntry] (v8: keyed by [exerciseEntryId],
 * was `loggedItemId`). Holds BOTH a prescription and a performance so one polymorphic row serves all
 * modalities:
 * - **Actuals** ([reps]/[loadKg]/[timeSec]/[distanceM]/[calories]) — what was performed.
 * - **Targets** ([targetReps]/…/[targetCalories]) — the prescription; in a template only targets
 *   are set. Which pair is meaningful is decided by the parent exercise's metric.
 */
@Entity(tableName = "set_entries", indices = [Index("exerciseEntryId")])
data class SetEntry(
    @PrimaryKey val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    // Performance (actuals):
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val rpe: Int? = null,
    // Prescription (targets):
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val targetTimeSec: Int? = null,
    val targetDistanceM: Int? = null,
    // v8 additions:
    val calories: Int? = null,
    val targetCalories: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

/** Per-session training volume (Σ reps × loadKg over strength sets) — a plain query-result POJO. */
data class SessionVolume(
    val sessionId: String,
    val volume: Double,
)
