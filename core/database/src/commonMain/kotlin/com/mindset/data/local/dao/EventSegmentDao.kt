package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.EventSegmentEntity

@Dao
interface EventSegmentDao {
    @Query("SELECT COUNT(*) FROM event_segment")
    suspend fun segmentCount(): Int

    @Query("SELECT * FROM event_segment WHERE formatKey = :formatKey ORDER BY orderIndex")
    suspend fun segmentsForFormat(formatKey: String): List<EventSegmentEntity>

    @Upsert
    suspend fun upsertSegments(rows: List<EventSegmentEntity>)
}
