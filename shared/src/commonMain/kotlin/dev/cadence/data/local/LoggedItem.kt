package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * An exercise placed within a session (one card in Log Workout). `orderIndex` preserves the order
 * the user added them. No DB-level foreign keys: deletes are managed explicitly in the repository
 * (and the sync pull replaces a session's children wholesale), so an FK/cascade would add pragma
 * setup for no benefit. Indexed by `sessionId` for the per-session query.
 */
@Entity(tableName = "logged_items", indices = [Index("sessionId")])
data class LoggedItem(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val orderIndex: Int,
)

/**
 * A logged item paired with its sets — the shape Log Workout renders per card. Deliberately a
 * PLAIN data class (no Room `@Relation`): Room's relation POJOs proved brittle under KSP in
 * commonMain, so the repository composes this from two simple queries instead, which is robust
 * across targets and keeps the join logic explicit.
 */
data class LoggedItemWithSets(
    val item: LoggedItem,
    val sets: List<SetEntry>,
)

@Dao
interface LoggedItemDao {
    @Insert
    suspend fun insert(item: LoggedItem)

    @Query("SELECT * FROM logged_items WHERE sessionId = :sessionId ORDER BY orderIndex")
    fun observeForSession(sessionId: String): Flow<List<LoggedItem>>

    @Query("SELECT * FROM logged_items WHERE sessionId = :sessionId ORDER BY orderIndex")
    suspend fun getBySession(sessionId: String): List<LoggedItem>

    @Query("SELECT COUNT(*) FROM logged_items WHERE sessionId = :sessionId")
    suspend fun countForSession(sessionId: String): Int

    @Query("DELETE FROM logged_items WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
