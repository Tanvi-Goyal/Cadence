@file:OptIn(ExperimentalTime::class)

package com.mindset.domain.repository

import com.mindset.model.RaceGoal
import com.mindset.model.RaceGoalStatus
import com.mindset.model.RaceMode
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Reads and writes the athlete's [RaceGoal]s. The UI observes goals as [Flow]s and never sees storage
 * details. Writes follow the project sync discipline: the impl stamps a UUIDv7 id + `createdAt`/
 * `updatedAt`, and enqueues an outbox row in the same transaction (sync-ready though v1 has no server).
 */
interface RaceGoalRepository {
    /** The next race the athlete is training for — the soonest [RaceGoalStatus.UPCOMING] goal, or null. */
    fun observeUpcoming(): Flow<RaceGoal?>

    /** All goals, newest first — past ([RaceGoalStatus.COMPLETED]) and upcoming. */
    fun observeAll(): Flow<List<RaceGoal>>

    /** Creates and persists a new goal (impl assigns id + timestamps + outbox); returns it. */
    suspend fun create(
        formatKey: String,
        divisionKey: String,
        mode: RaceMode,
        targetDate: Instant?,
        city: String?,
        goalTimeSec: Int? = null,
    ): RaceGoal

    /** Transitions a goal's lifecycle (e.g. UPCOMING → COMPLETED after race day). */
    suspend fun updateStatus(id: String, status: RaceGoalStatus)
}
