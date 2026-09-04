package com.mindset.model

enum class HyroxVariant { FULL, FIRST_HALF, SECOND_HALF, HALVED }

enum class HyroxStationType { RUN, STATION }

data class HyroxStationModel(
    val index: Int, // 0-based position in the flattened run→station sequence
    val stationType: HyroxStationType,
    val station: HyroxStation?, // the station enum (null for runs) — drives the UI glyph
    val exerciseId: String, // catalog exercise id backing the synthesized entry
    val title: String, // "Run 1" / "1. SkiErg"
    val detail: String, // "1000m Distance" / "6kg Ball • 3m Target"
    val value: String, // "1.0 km" / "1000m" / "152 kg" / "100 reps"
    val targetDistanceM: Int? = null,
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val loadDisplay: String? = null, // rulebook weight string ("102 kg" / "2×24 kg"); null = unweighted
    val segmentKey: String? = null,
)

data class HyroxDivisionInfo(val key: String, val label: String)
