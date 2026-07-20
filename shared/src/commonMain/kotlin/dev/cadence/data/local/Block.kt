package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * A structural grouping of exercises within a session — straight / superset / circuit / interval /
 * run (see `dev.cadence.model.BlockType`, stored here as its `name` TEXT). New in v8: pre-v8
 * sessions had a flat exercise list; blocks let a session express "3 rounds of A+B+C".
 *
 * No DB-level foreign keys (same rationale as [LoggedItem]); indexed by `sessionId`. Carries the
 * sync envelope (`createdAt`/`updatedAt`/`deletedAt`) like every user-owned entity.
 *
 * NOTE (A3): added additively and not yet written by any code path — the repository/sync cutover to
 * the block graph happens in A4; the legacy-fold migration in A6.
 */
@Entity(tableName = "blocks", indices = [Index("sessionId")])
data class Block(
    @PrimaryKey val id: String,
    val sessionId: String,
    val type: String,
    val orderIndex: Int,
    val rounds: Int? = null,
    val restBetweenRoundsMs: Long? = null,
    val label: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Dao
interface BlockDao {
    @Insert
    suspend fun insert(block: Block)

    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY orderIndex")
    fun observeForSession(sessionId: String): Flow<List<Block>>

    @Query("SELECT * FROM blocks WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY orderIndex")
    suspend fun getBySession(sessionId: String): List<Block>

    @Query("DELETE FROM blocks WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
