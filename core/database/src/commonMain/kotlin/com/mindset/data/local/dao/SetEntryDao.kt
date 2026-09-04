package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.mindset.data.local.SessionDuration
import com.mindset.data.local.SessionVolume
import com.mindset.data.local.SetEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetEntryDao {
    @Insert
    suspend fun insert(set: SetEntryEntity)

    @Update
    suspend fun update(set: SetEntryEntity)

    @Query(
        "SELECT * FROM set_entries WHERE exerciseEntryId = :exerciseEntryId AND deletedAt IS NULL ORDER BY setNumber",
    )
    suspend fun getForEntry(exerciseEntryId: String): List<SetEntryEntity>

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
    suspend fun lastSetsForExercise(exerciseId: String, excludeSessionId: String): List<SetEntryEntity>

    @Query(
        """
        SELECT s.* FROM set_entries s
        INNER JOIN exercise_entries e ON s.exerciseEntryId = e.id
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE b.sessionId = :sessionId AND s.deletedAt IS NULL
        ORDER BY s.setNumber
        """,
    )
    fun observeForSession(sessionId: String): Flow<List<SetEntryEntity>>

    @Query("DELETE FROM set_entries WHERE exerciseEntryId IN (:entryIds)")
    suspend fun deleteForEntries(entryIds: List<String>)

    /** Soft-delete (tombstone) every set under an entry — pairs with [ExerciseEntryDao.softDelete]. */
    @Query(
        "UPDATE set_entries SET deletedAt = :now, updatedAt = :now WHERE exerciseEntryId = :entryId AND deletedAt IS NULL",
    )
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

    /**
     * Training time per session (Σ of every logged split, in seconds), reactive — the list rows'
     * duration metric. Deliberately unfiltered by segment kind, so **run** splits count alongside
     * station splits; the `timeSec IS NOT NULL` guard drops target-only (ghost) sets by construction.
     * Replaces `finishedAt − startedAt`, which measured wall clock rather than training time.
     */
    @Query(
        """
        SELECT b.sessionId AS sessionId, COALESCE(SUM(s.timeSec), 0) AS durationSec
        FROM set_entries s
        INNER JOIN exercise_entries e ON s.exerciseEntryId = e.id
        INNER JOIN blocks b ON e.blockId = b.id
        WHERE s.timeSec IS NOT NULL AND s.deletedAt IS NULL AND e.deletedAt IS NULL AND b.deletedAt IS NULL
        GROUP BY b.sessionId
        """,
    )
    fun observeSessionDurations(): Flow<List<SessionDuration>>
}
