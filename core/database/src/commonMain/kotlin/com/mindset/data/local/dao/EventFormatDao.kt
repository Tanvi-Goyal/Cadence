package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.EventFormatEntity

@Dao
interface EventFormatDao {
    @Upsert suspend fun upsertFormats(rows: List<EventFormatEntity>)

    @Query("SELECT * FROM event_format")
    suspend fun formats(): List<EventFormatEntity>
}
