package dev.cadence.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * A tiny key/value table for sync bookkeeping — currently just the pull cursor. Reuses Room
 * rather than pulling in DataStore for a single value. Keyed by a string so more sync state can
 * be added later without a schema change.
 */
@Entity(tableName = "sync_meta")
data class SyncMeta(
    @PrimaryKey val key: String,
    val value: String,
)

@Dao
interface SyncMetaDao {
    @Query("SELECT value FROM sync_meta WHERE key = :key")
    suspend fun get(key: String): String?

    @Upsert
    suspend fun set(meta: SyncMeta)
}

/** Well-known keys for [SyncMeta]. */
object SyncMetaKeys {
    const val PULL_CURSOR = "pull_cursor"
}
