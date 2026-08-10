package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "segment_standard",
    indices = [Index("formatKey"), Index("divisionKey"), Index("segmentId")],
)
data class EventSegmentStandardEntity(
    @PrimaryKey val id: String, // "HYROX:MEN:04-sled-push"
    val formatKey: String,
    val divisionKey: String,
    val segmentId: String,
    val mode: String, // com.mindset.model.RaceMode name (v1: SINGLES)
    val loadKg: Double? = null, // canonical weight (e.g. 152.0)
    val loadDisplay: String? = null, // rulebook string, e.g. "2×24 kg"
    val targetReps: Int? = null, // division-specific reps (e.g. wall balls 100)
    val targetDistanceM: Int? = null, // division-specific distance override
    val targetHeightM: Double? = null, // wall-ball target height (2.7 / 3.0)
)
