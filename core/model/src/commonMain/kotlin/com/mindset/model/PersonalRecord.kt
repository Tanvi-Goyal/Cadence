@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A cached personal record for an exercise. Cached for a fast PB screen + the completion toast, but
 * always **derivable** from history (see the `rebuildPrs` recompute path) so a lost cache is never
 * a correctness problem.
 *
 * [value] is interpreted per [kind]: estimated-1RM kg, weight kg, reps, seconds, or calories.
 * [distanceBucketM] is set only for [PrKind.BEST_TIME] (e.g. best time at the 1000 m bucket).
 * [sourceSetId] links back to the set that produced the record.
 */
data class PersonalRecord(
    override val id: String,
    val exerciseId: String,
    val kind: PrKind,
    val value: Double,
    val distanceBucketM: Int?,
    /** v12: weight-class key (an `event_division.key`) for a loaded station's PB; null = division-agnostic. */
    val divisionKey: String? = null,
    val achievedAt: Instant,
    val sourceSetId: String,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
