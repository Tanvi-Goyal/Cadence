package com.mindset.presentation

data class StationOption(
    val segmentKey: String,
    val name: String, // e.g. "2. Sled Push"
    val standard: String, // division-accurate readout, e.g. "152 kg" / "1000m" / "100 reps"
)
