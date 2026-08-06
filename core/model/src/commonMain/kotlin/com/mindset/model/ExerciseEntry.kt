@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * One exercise slotted into a [Block] (the v8 successor to the pre-v8 `LoggedItem`, now parented by
 * [blockId] instead of the session). [exerciseId] references a catalog [Exercise]; [targetSets] and
 * [restMs] are the prescription hints carried from a template.
 */
data class ExerciseEntry(
    override val id: String,
    val blockId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int?,
    val restMs: Long?,
    /** v9: a coaching cue for this exercise, and whether the prescription is per-side ("ES"). */
    val note: String? = null,
    val eachSide: Boolean = false,
    /** v12: the `event_segment.id` this entry fulfils when it belongs to a race sim (null otherwise). */
    val segmentKey: String? = null,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
