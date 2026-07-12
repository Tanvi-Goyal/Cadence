package dev.cadence.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Reads/writes for [Session]. [observeAll] returns a [Flow] so the UI observes the DB reactively
 * and never has to poll — the single-source-of-truth contract in AGENTS.md.
 */
@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE deleted = 0 ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<Session>>

    @Insert
    suspend fun insert(session: Session)

    /** Insert-or-replace, used by the pull path to apply a remote version of a row. */
    @Upsert
    suspend fun upsert(session: Session)

    /** Read one row (may be soft-deleted) — the sync engine needs it to apply Last-Write-Wins. */
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): Session?

    /** Mark a set of sessions as synced after a successful push. */
    @Query("UPDATE sessions SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun markStatus(ids: List<String>, status: String)
}
