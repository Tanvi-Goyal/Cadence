package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.SyncMeta

@Dao
interface SyncMetaDao {
    @Query("SELECT value FROM sync_meta WHERE key = :key")
    suspend fun get(key: String): String?

    @Upsert
    suspend fun set(meta: SyncMeta)
}
