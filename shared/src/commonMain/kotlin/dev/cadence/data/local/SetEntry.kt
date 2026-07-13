package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

/**
 * One logged set. Unified for both modalities (PRD §4): strength uses [reps]/[loadKg], conditioning
 * uses [timeSec]/[distanceM]; the others are null. Which pair is meaningful is decided by the
 * parent exercise's [Exercise.metric].
 */
@Entity(tableName = "set_entries", indices = [Index("loggedItemId")])
data class SetEntry(
    @PrimaryKey val id: String,
    val loggedItemId: String,
    val setNumber: Int,
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val rpe: Int? = null,
)

/** Per-session training volume (Σ reps × loadKg over strength sets) — a plain query-result POJO. */
data class SessionVolume(
    val sessionId: String,
    val volume: Double,
)

@Dao
interface SetEntryDao {
    @Insert
    suspend fun insert(set: SetEntry)

    @Update
    suspend fun update(set: SetEntry)

    @Query("SELECT * FROM set_entries WHERE loggedItemId = :loggedItemId ORDER BY setNumber")
    suspend fun getForLoggedItem(loggedItemId: String): List<SetEntry>

    /** All sets belonging to a session (joined via logged_items) — observed by Log Workout. */
    @Query(
        """
        SELECT s.* FROM set_entries s
        INNER JOIN logged_items li ON s.loggedItemId = li.id
        WHERE li.sessionId = :sessionId
        ORDER BY s.setNumber
        """,
    )
    fun observeForSession(sessionId: String): Flow<List<SetEntry>>

    @Query("DELETE FROM set_entries WHERE loggedItemId IN (:loggedItemIds)")
    suspend fun deleteForLoggedItems(loggedItemIds: List<String>)

    /** Strength volume per session, for Home's Volume stat + per-row metric. Reactive. */
    @Query(
        """
        SELECT li.sessionId AS sessionId, COALESCE(SUM(s.reps * s.loadKg), 0) AS volume
        FROM set_entries s
        INNER JOIN logged_items li ON s.loggedItemId = li.id
        WHERE s.reps IS NOT NULL AND s.loadKg IS NOT NULL
        GROUP BY li.sessionId
        """,
    )
    fun observeSessionVolumes(): Flow<List<SessionVolume>>
}
