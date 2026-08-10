package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.EventDivisionEntity

@Dao
interface EventDivisionDao {
    @Upsert
    suspend fun upsertDivisions(rows: List<EventDivisionEntity>)

    @Query("SELECT * FROM event_division WHERE formatKey = :formatKey ORDER BY orderIndex")
    suspend fun divisionsForFormat(formatKey: String): List<EventDivisionEntity>
}
