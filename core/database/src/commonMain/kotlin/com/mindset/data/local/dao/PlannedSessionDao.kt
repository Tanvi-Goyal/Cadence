package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.PlannedSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedSessionDao {
    /** The current plan (at most one for v1), observed by Home. */
    @Query("SELECT * FROM planned_sessions LIMIT 1")
    fun observeCurrent(): Flow<PlannedSession?>

    @Query("SELECT * FROM planned_sessions LIMIT 1")
    suspend fun getCurrent(): PlannedSession?

    @Upsert
    suspend fun upsert(plan: PlannedSession)
}
