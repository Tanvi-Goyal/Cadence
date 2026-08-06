package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.EventDivisionEntity
import com.mindset.data.local.EventFormatEntity
import com.mindset.data.local.EventSegmentEntity
import com.mindset.data.local.SegmentStandardEntity

@Dao
interface EventRefDao {
    @Query("SELECT COUNT(*) FROM event_segment")
    suspend fun segmentCount(): Int

    @Upsert suspend fun upsertFormats(rows: List<EventFormatEntity>)

    @Upsert suspend fun upsertSegments(rows: List<EventSegmentEntity>)

    @Upsert suspend fun upsertDivisions(rows: List<EventDivisionEntity>)

    @Upsert suspend fun upsertStandards(rows: List<SegmentStandardEntity>)

    @Query("SELECT * FROM event_format")
    suspend fun formats(): List<EventFormatEntity>

    @Query("SELECT * FROM event_segment WHERE formatKey = :formatKey ORDER BY orderIndex")
    suspend fun segmentsForFormat(formatKey: String): List<EventSegmentEntity>

    @Query("SELECT * FROM event_division WHERE formatKey = :formatKey ORDER BY orderIndex")
    suspend fun divisionsForFormat(formatKey: String): List<EventDivisionEntity>

    @Query(
        "SELECT * FROM segment_standard WHERE formatKey = :formatKey AND divisionKey = :divisionKey AND mode = :mode",
    )
    suspend fun standardsFor(
        formatKey: String,
        divisionKey: String,
        mode: String,
    ): List<SegmentStandardEntity>
}
