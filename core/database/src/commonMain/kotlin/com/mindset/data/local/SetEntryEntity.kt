package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "set_entries", indices = [Index("exerciseEntryId")])
data class SetEntryEntity(
    @PrimaryKey val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val calories: Int? = null,
    val rpe: Int? = null,
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val targetTimeSec: Int? = null,
    val targetDistanceM: Int? = null,
    val targetCalories: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

data class SessionVolume(val sessionId: String, val volume: Double)

/** Per-session total of the logged splits, in seconds — the projection behind Total Time. */
data class SessionDuration(val sessionId: String, val durationSec: Int)
