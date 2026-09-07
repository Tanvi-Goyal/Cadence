package com.mindset.model

/** An exercise that has logged history — populates the Stats chart selector. */
data class ExerciseRef(val id: String, val name: String)

/** One (session time, volume) sample for the per-exercise trend chart. */
data class VolumePoint(val startedAt: Long, val volume: Double)
