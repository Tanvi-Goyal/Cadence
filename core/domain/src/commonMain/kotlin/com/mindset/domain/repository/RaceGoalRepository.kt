@file:OptIn(ExperimentalTime::class)

package com.mindset.domain.repository

import com.mindset.model.RaceGoal
import com.mindset.model.RaceGoalStatus
import com.mindset.model.RaceMode
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface RaceGoalRepository {
    fun observeUpcoming(): Flow<RaceGoal?>

    fun observeAll(): Flow<List<RaceGoal>>

    suspend fun create(
        formatKey: String,
        divisionKey: String,
        mode: RaceMode,
        targetDate: Instant?,
        city: String?,
        goalTimeSec: Int? = null,
    ): RaceGoal

    suspend fun updateStatus(id: String, status: RaceGoalStatus)
}
