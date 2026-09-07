package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.OutboxEntry

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(entry: OutboxEntry)

    /** One row per session (deterministic id): repeated edits refresh rather than pile up. */
    @Upsert
    suspend fun upsert(entry: OutboxEntry)

    @Query("SELECT COUNT(*) FROM outbox")
    suspend fun count(): Int

    /** All pending mutations, oldest first — the sync engine drains these in order. */
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    suspend fun getAll(): List<OutboxEntry>

    /** Remove entries once their push is confirmed (done in the same txn that marks rows synced). */
    @Query("DELETE FROM outbox WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
