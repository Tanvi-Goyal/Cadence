package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * An exercise slotted into a [Block] — the v8 successor to the pre-v8 `LoggedItem`, now parented by
 * [blockId] instead of the session directly. No DB-level foreign keys (deletes are managed in the
 * repository, and sync replaces a session's children wholesale). Carries the sync envelope; indexed
 * by [blockId] (per-block reads) and [exerciseId] (stats/PB joins).
 */
@Entity(
    tableName = "exercise_entries",
    indices = [Index("blockId"), Index("exerciseId")],
)
data class ExerciseEntry(
    @PrimaryKey val id: String,
    val blockId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int? = null,
    val restMs: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Dao
interface ExerciseEntryDao {
    @Insert
    suspend fun insert(entry: ExerciseEntry)

    @Query("SELECT COUNT(*) FROM exercise_entries WHERE blockId = :blockId AND deletedAt IS NULL")
    suspend fun countForBlock(blockId: String): Int

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
}
