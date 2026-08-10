package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.mindset.model.ExerciseRef
import com.mindset.model.VolumePoint
import kotlinx.coroutines.flow.Flow

/**
 * Read-only aggregate queries backing the Stats screen. Plain column-mapped result POJOs (NOT Room
 * `@Relation`, which breaks KSP in commonMain). No new tables — these read over the existing
 * sessions/logged_items/set_entries, so no schema-version bump.
 */
@Dao
interface StatsDao {
    /** Exercises that appear in at least one session (so the selector never offers empty charts). */
    @Query(
        """
        SELECT DISTINCT e.id AS id, e.name AS name
        FROM exercises e
        INNER JOIN exercise_entries ee ON ee.exerciseId = e.id
        INNER JOIN blocks b ON ee.blockId = b.id
        INNER JOIN sessions s ON b.sessionId = s.id
        WHERE s.isTemplate = 0 AND s.deletedAt IS NULL
        ORDER BY e.name
        """,
    )
    fun exercisesWithHistory(): Flow<List<ExerciseRef>>

    /** Strength volume for one exercise, one point per session, oldest → newest. */
    @Query(
        """
        SELECT s.startedAt AS startedAt, COALESCE(SUM(se.reps * se.loadKg), 0) AS volume
        FROM set_entries se
        INNER JOIN exercise_entries ee ON se.exerciseEntryId = ee.id
        INNER JOIN blocks b ON ee.blockId = b.id
        INNER JOIN sessions s ON b.sessionId = s.id
        WHERE ee.exerciseId = :exerciseId
          AND se.reps IS NOT NULL AND se.loadKg IS NOT NULL
          AND s.deletedAt IS NULL AND s.isTemplate = 0
        GROUP BY s.id
        ORDER BY s.startedAt ASC
        """,
    )
    fun volumeOverTime(exerciseId: String): Flow<List<VolumePoint>>
}
