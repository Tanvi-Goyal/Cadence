package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A training session — the core unit the app logs.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val name: String = "Session",
    val type: String = SessionType.STRENGTH,
    val notes: String? = null,
    val isTemplate: Boolean = false,
    val source: String = SessionSource.MANUAL,
    val templateId: String? = null,
    val category: String? = null,
    val focus: String? = null,
    val programWeek: Int? = null,
    val finishedAt: Long? = null,
    val raceGoalId: String? = null,
    val formatKey: String? = null,
    val divisionKey: String? = null,
    val avgHeartRate: Int? = null,
    val caloriesKcal: Int? = null,
    val perceivedEffort: Int? = null,
    val updatedAt: Long,
    val syncStatus: String = SyncStatus.PENDING,
    val createdAt: Long = 0L,
    val deletedAt: Long? = null,
)

object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}

object SessionType {
    const val STRENGTH = "STRENGTH"
    const val CONDITIONING = "CONDITIONING"
    const val HYROX = "HYROX"
    const val MIXED = "MIXED"
}

object SessionSource {
    const val MANUAL = "MANUAL"
    const val FROM_TEMPLATE = "FROM_TEMPLATE"
    const val RACE_SIM = "RACE_SIM"
    const val HEALTH_CONNECT = "HEALTH_CONNECT"
}
