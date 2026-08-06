package dev.cadence.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * A cached personal record (new in v8). Cached for a fast PB screen + completion toast but always
 * recomputable from history. `kind` is `dev.cadence.model.PrKind.name`; `value` is interpreted per
 * kind (e1RM kg / weight kg / reps / seconds / calories); `distanceBucketM` is set only for
 * BEST_TIME. Indexed for the "current PR of this kind (and bucket)" lookup.
 *
 * NOTE (A3): added additively; PB detection writes these rows in A5.
 */
@Entity(
    tableName = "personal_records",
    indices = [Index("exerciseId"), Index("exerciseId", "kind", "divisionKey", "distanceBucketM")],
)
data class PersonalRecord(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val kind: String,
    val value: Double,
    val distanceBucketM: Int? = null,
    // v12: weight-class dimension — an `event_division.key` (e.g. "MEN"). A loaded station's PB is
    // per-division ("Sled Push @ Men" ≠ "@ Women Pro"); null for division-agnostic PBs (bodyweight,
    // free-weight lifts, runs). Part of the "current PR of this kind (+ division + bucket)" lookup key.
    val divisionKey: String? = null,
    val achievedAt: Long,
    val sourceSetId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)
