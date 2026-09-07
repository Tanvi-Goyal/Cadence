package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * An exercise slotted into a [BlockEntity] — the v8 successor to the pre-v8 `LoggedItem`, now parented by
 * [blockId] instead of the session directly. No DB-level foreign keys (deletes are managed in the
 * repository, and sync replaces a session's children wholesale). Carries the sync envelope; indexed
 * by [blockId] (per-block reads) and [exerciseId] (stats/PB joins).
 */
@Entity(
    tableName = "exercise_entries",
    indices = [Index("blockId"), Index("exerciseId")],
)
data class ExerciseEntryEntity(
    @PrimaryKey val id: String,
    val blockId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int? = null,
    val restMs: Long? = null,
    val eachSide: Boolean? = null,
    val segmentKey: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)
