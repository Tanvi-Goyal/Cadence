package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert

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

    /** Version of the seeded reference catalog on this device; bump to re-import (see repository). */
    const val SEED_VERSION = "seed_version"

    /** Version of the seeded program templates on this device; bump to re-seed (see repository). */
    const val TEMPLATE_SEED_VERSION = "template_seed_version"

    /** Version of the seeded HYROX reference tables on this device; bump to re-seed (see repository). */
    const val HYROX_SEED_VERSION = "hyrox_seed_version"
}
