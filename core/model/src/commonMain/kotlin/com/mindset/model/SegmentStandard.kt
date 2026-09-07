package com.mindset.model

/**
 * Per-division parameters (loads / reps / targets) for one [EventSegment] in one [RaceMode]. This is
 * where "Sled Push · Men · 152 kg" and "Wall Balls · Men · 100 reps @ 3 m" live. v1 seeds
 * [RaceMode.SINGLES] only; Doubles/Relay standards are additive rows later.
 */
data class SegmentStandard(
    val id: String,
    val formatKey: String,
    val divisionKey: String,
    val segmentId: String,
    val mode: RaceMode = RaceMode.SINGLES,
    val loadKg: Double? = null,
    val loadDisplay: String? = null,
    val targetReps: Int? = null,
    val targetDistanceM: Int? = null,
    val targetHeightM: Double? = null,
)
