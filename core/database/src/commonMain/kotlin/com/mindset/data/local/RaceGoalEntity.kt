package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
