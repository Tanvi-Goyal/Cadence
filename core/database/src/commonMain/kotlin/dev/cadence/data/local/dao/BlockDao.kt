package dev.cadence.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.cadence.data.local.Block
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    /** IGNORE on conflict: the deterministic implicit block is inserted-if-absent by `addExercise`. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(block: Block)

    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY orderIndex")
    fun observeForSession(sessionId: String): Flow<List<Block>>

    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY orderIndex")
    suspend fun getBySession(sessionId: String): List<Block>

    @Query("DELETE FROM blocks WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
