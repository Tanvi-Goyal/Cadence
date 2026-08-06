package dev.cadence.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import dev.cadence.data.local.ExerciseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDao {
    @Insert
    suspend fun insert(entry: ExerciseEntry)

    @Query("SELECT COUNT(*) FROM exercise_entries WHERE blockId = :blockId AND deletedAt IS NULL")
    suspend fun countForBlock(blockId: String): Int

    @Query("SELECT * FROM exercise_entries WHERE id = :id")
    suspend fun getById(id: String): ExerciseEntry?

    /** All entries in a session, joined through its blocks and ordered block-then-entry — reactive. */
    @Query(
        """
        SELECT e.* FROM exercise_entries e
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE b.sessionId = :sessionId AND e.deletedAt IS NULL AND b.deletedAt IS NULL
        ORDER BY b.orderIndex, e.orderIndex
        """,
    )
    fun observeBySession(sessionId: String): Flow<List<ExerciseEntry>>

    /** Suspend variant of [observeBySession] — for the deep-copy and sync paths. */
    @Query(
        """
        SELECT e.* FROM exercise_entries e
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE b.sessionId = :sessionId AND e.deletedAt IS NULL AND b.deletedAt IS NULL
        ORDER BY b.orderIndex, e.orderIndex
        """,
    )
    suspend fun getBySession(sessionId: String): List<ExerciseEntry>

    /** Hard-remove every entry belonging to a session's blocks — used by the wholesale sync replace. */
    @Query("DELETE FROM exercise_entries WHERE blockId IN (SELECT id FROM blocks WHERE sessionId = :sessionId)")
    suspend fun deleteBySession(sessionId: String)

    /** Soft-delete (tombstone) one entry — when the user removes an item from a live session; the
     *  parent session is touched separately so the deletion re-syncs. */
    @Query("UPDATE exercise_entries SET deletedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)
}
