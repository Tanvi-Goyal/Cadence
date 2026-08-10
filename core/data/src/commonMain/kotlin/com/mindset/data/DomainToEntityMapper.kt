package com.mindset.data

import com.mindset.data.local.SetEntryEntity
import com.mindset.model.SetEntry

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
