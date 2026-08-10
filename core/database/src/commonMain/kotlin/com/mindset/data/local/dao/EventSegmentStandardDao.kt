package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.EventSegmentStandardEntity

@Dao
interface EventSegmentStandardDao {
    @Query(
        "SELECT * FROM segment_standard WHERE formatKey = :formatKey AND divisionKey = :divisionKey AND mode = :mode",
    )
    suspend fun standardsFor(formatKey: String, divisionKey: String, mode: String): List<EventSegmentStandardEntity>

    @Upsert suspend fun upsertStandards(rows: List<EventSegmentStandardEntity>)
}
