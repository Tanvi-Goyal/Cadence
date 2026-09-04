package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.mindset.data.local.SessionDuration
import com.mindset.model.StationRecentRow
import com.mindset.model.StationSessionCountRow
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

    /**
     * Per-station session count for one division — the board's SESSIONS metric. Counts a session only
     * when it logged an **actual** time for that station, so template/ghost target-only rows never
     * inflate it (`timeSec IS NOT NULL`), and `COUNT(DISTINCT sessionId)` keeps a session logged twice
     * in one workout from counting twice. Scoped to [divisionKey] so the whole card reads at one race
     * weight; sessions predating division stamping carry a null `divisionKey` and are excluded.
     */
    @Query(
        """
        SELECT e.hyroxStation AS hyroxStation, COUNT(DISTINCT b.sessionId) AS sessionCount
        FROM set_entries s
        INNER JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        INNER JOIN blocks b ON ee.blockId = b.id
        INNER JOIN sessions ss ON b.sessionId = ss.id
        INNER JOIN exercises e ON ee.exerciseId = e.id
        WHERE e.hyroxStation IS NOT NULL
          AND s.timeSec IS NOT NULL AND s.timeSec > 0
          AND s.deletedAt IS NULL AND ee.deletedAt IS NULL AND b.deletedAt IS NULL
          AND ss.deletedAt IS NULL AND ss.isTemplate = 0
          AND ss.divisionKey = :divisionKey
        GROUP BY e.hyroxStation
        """,
    )
    fun observeStationSessionCounts(divisionKey: String): Flow<List<StationSessionCountRow>>

    /**
     * Every logged station split for one division, newest session first — the board's RECENT metric.
     * Deliberately NOT a `GROUP BY` with a bare column beside `MAX(startedAt)`: that leans on a
     * SQLite-specific rule about which row a bare column comes from, so the repository takes the
     * first row per station instead, which is obviously correct. Bounded by the 8 stations × the
     * athlete's own history, and ordered in SQL so the repository does no sorting.
     */
    @Query(
        """
        SELECT e.hyroxStation AS hyroxStation, b.sessionId AS sessionId,
               s.timeSec AS timeSec, ss.startedAt AS startedAt
        FROM set_entries s
        INNER JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        INNER JOIN blocks b ON ee.blockId = b.id
        INNER JOIN sessions ss ON b.sessionId = ss.id
        INNER JOIN exercises e ON ee.exerciseId = e.id
        WHERE e.hyroxStation IS NOT NULL
          AND s.timeSec IS NOT NULL AND s.timeSec > 0
          AND s.deletedAt IS NULL AND ee.deletedAt IS NULL AND b.deletedAt IS NULL
          AND ss.deletedAt IS NULL AND ss.isTemplate = 0
          AND ss.divisionKey = :divisionKey
        ORDER BY e.hyroxStation, ss.startedAt DESC, s.setNumber DESC
        """,
    )
    fun observeStationRecents(divisionKey: String): Flow<List<StationRecentRow>>
}
