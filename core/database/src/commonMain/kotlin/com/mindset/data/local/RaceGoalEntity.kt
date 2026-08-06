package com.mindset.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * The athlete's target race(s) — first-class and multi-instance (v12), replacing the single target
 * that used to live on `athlete_profile`. Carries the full sync envelope + local `syncStatus` (like
 * [Session]); writes enqueue an outbox row so Phase-2 sync adopts it with no schema break. Home reads
 * the next UPCOMING goal (soonest `targetDate`, open-ended goals last). `formatKey`/`divisionKey`
 * reference the seeded event tables; enum-ish columns are `.name` TEXT.
 */
@Entity(tableName = "race_goal", indices = [Index("status", "targetDate")])
data class RaceGoalEntity(
    @PrimaryKey val id: String,
    val formatKey: String,
    val divisionKey: String,
    val mode: String,
    val targetDate: Long? = null,
    val city: String? = null,
    val goalTimeSec: Int? = null,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val syncStatus: String = SyncStatus.PENDING,
)
