package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * An exercise slotted into a [Block] — the v8 successor to the pre-v8 `LoggedItem`, now parented by
 * [blockId] instead of the session directly. No DB-level foreign keys (deletes are managed in the
 * repository, and sync replaces a session's children wholesale). Carries the sync envelope; indexed
 * by [blockId] (per-block reads) and [exerciseId] (stats/PB joins).
 */
@Entity(
    tableName = "exercise_entries",
    indices = [Index("blockId"), Index("exerciseId")],
)
data class ExerciseEntry(
    @PrimaryKey val id: String,
    val blockId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int? = null,
    val restMs: Long? = null,
    // v9: a coaching cue ("50-55% of PR, focus on depth", "7+7+7 x2", rep ranges/tempo) and whether
    // the prescription is per-side (the pervasive "ES"). `eachSide` is nullable (null = false) so the
    // migration is a plain nullable ADD COLUMN.
    val note: String? = null,
    val eachSide: Boolean? = null,
    // v12: tags a race entry to the format segment it fulfils (an `event_segment.id`) so the race
    // timeline, per-segment PBs, and compromised-running splits are ordered reads instead of an
    // inference from orderIndex+exerciseId. Null on ordinary (non-race) entries.
    val segmentKey: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)
