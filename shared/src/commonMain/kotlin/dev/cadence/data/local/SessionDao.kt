package dev.cadence.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
