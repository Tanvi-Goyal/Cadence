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
 * One set — the single unit of work — under an [ExerciseEntry] (v8: keyed by [exerciseEntryId],
 * was `loggedItemId`). Holds BOTH a prescription and a performance so one polymorphic row serves all
 * modalities:
 * - **Actuals** ([reps]/[loadKg]/[timeSec]/[distanceM]/[calories]) — what was performed.
 * - **Targets** ([targetReps]/…/[targetCalories]) — the prescription; in a template only targets
 *   are set. Which pair is meaningful is decided by the parent exercise's metric.
 */
@Entity(tableName = "set_entries", indices = [Index("exerciseEntryId")])
data class SetEntry(
    @PrimaryKey val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    // Performance (actuals):
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val rpe: Int? = null,
    // Prescription (targets):
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val targetTimeSec: Int? = null,
    val targetDistanceM: Int? = null,
    // v8 additions:
    val calories: Int? = null,
    val targetCalories: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
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

    @Query("SELECT * FROM set_entries WHERE exerciseEntryId = :exerciseEntryId AND deletedAt IS NULL ORDER BY setNumber")
    suspend fun getForEntry(exerciseEntryId: String): List<SetEntry>

    /**
     * The actual-bearing sets of the **most recent prior session** that logged [exerciseId] — the raw
     * data behind "prefill from last time". The inner subquery picks that one session id (newest
     * `startedAt`, excluding [excludeSessionId] and templates, and only sessions with a real actual);
     * the outer query returns just that session's logged sets in set order. Bounded read (one
     * session's sets); the joins ride the `exerciseEntryId`/`blockId` indices. Empty when the exercise
     * has never been logged before.
     */
    @Query(
        """
        SELECT s.* FROM set_entries s
        INNER JOIN exercise_entries e ON s.exerciseEntryId = e.id
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE e.exerciseId = :exerciseId
          AND s.deletedAt IS NULL
          AND (s.reps IS NOT NULL OR s.loadKg IS NOT NULL OR s.timeSec IS NOT NULL OR s.distanceM IS NOT NULL)
          AND b.sessionId = (
            SELECT b2.sessionId FROM set_entries s2
            INNER JOIN exercise_entries e2 ON s2.exerciseEntryId = e2.id
            INNER JOIN blocks b2 ON e2.blockId = b2.id
            INNER JOIN sessions ss ON b2.sessionId = ss.id
            WHERE e2.exerciseId = :exerciseId
              AND ss.id <> :excludeSessionId
              AND ss.isTemplate = 0
              AND ss.deletedAt IS NULL
              AND s2.deletedAt IS NULL
              AND (s2.reps IS NOT NULL OR s2.loadKg IS NOT NULL OR s2.timeSec IS NOT NULL OR s2.distanceM IS NOT NULL)
            ORDER BY ss.startedAt DESC
            LIMIT 1
          )
        ORDER BY s.setNumber
        """,
    )
    suspend fun lastSetsForExercise(exerciseId: String, excludeSessionId: String): List<SetEntry>

    /** All sets in a session (joined via exercise_entries → blocks) — observed by Log Workout. */
    @Query(
        """
        SELECT s.* FROM set_entries s
        INNER JOIN exercise_entries e ON s.exerciseEntryId = e.id
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE b.sessionId = :sessionId AND s.deletedAt IS NULL
        ORDER BY s.setNumber
        """,
    )
    fun observeForSession(sessionId: String): Flow<List<SetEntry>>

    @Query("DELETE FROM set_entries WHERE exerciseEntryId IN (:entryIds)")
    suspend fun deleteForEntries(entryIds: List<String>)

    /** Soft-delete (tombstone) every set under an entry — pairs with [ExerciseEntryDao.softDelete]. */
    @Query("UPDATE set_entries SET deletedAt = :now, updatedAt = :now WHERE exerciseEntryId = :entryId AND deletedAt IS NULL")
    suspend fun softDeleteForEntry(entryId: String, now: Long)

    /**
     * Strength volume per session, for Home's Volume stat. Reactive. Template sets carry only
     * targets (null actuals), so the `IS NOT NULL` guards exclude them by construction.
     */
    @Query(
        """
        SELECT b.sessionId AS sessionId, COALESCE(SUM(s.reps * s.loadKg), 0) AS volume
        FROM set_entries s
        INNER JOIN exercise_entries e ON s.exerciseEntryId = e.id
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE s.reps IS NOT NULL AND s.loadKg IS NOT NULL AND s.deletedAt IS NULL
        GROUP BY b.sessionId
        """,
    )
    fun observeSessionVolumes(): Flow<List<SessionVolume>>
}
