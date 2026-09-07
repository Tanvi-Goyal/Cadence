package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "event_segment", indices = [Index("formatKey")])
data class EventSegmentEntity(
    @PrimaryKey val id: String, // slug, e.g. "hyrox:04-sled-push", "hyrox:03-run"
    val formatKey: String,
    val orderIndex: Int, // race order (Hyrox 1..16: run, station, run, station, …)
    val kind: String, // com.mindset.model.SegmentKind name (RUN/STATION/TRANSITION)
    val exerciseId: String, // → catalog Exercise id (hyrox-run, hyrox-ski-erg, …)
    val name: String,
    val label: String = "", // thematic grouping, e.g. "Block 1: Start"
    val metric: String, // com.mindset.model.MetricType name
    val distanceM: Int? = null, // fixed distance (null for rep-scored)
    val reps: Int? = null, // fixed reps (wall balls; null otherwise)
    val loadType: String? = null, // which per-division load applies (null = bodyweight/erg/run)
    val descriptor: String = "",
)
