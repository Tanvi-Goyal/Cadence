package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/** An exercise that has at least one logged set — used to populate the Stats chart selector. */
data class ExerciseRef(
    val id: String,
    val name: String,
)

/** One (session time, volume) sample for the per-exercise trend chart. */
data class VolumePoint(
    val startedAt: Long,
    val volume: Double,
)

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
        INNER JOIN logged_items li ON li.exerciseId = e.id
        ORDER BY e.name
        """,
    )
    fun exercisesWithHistory(): Flow<List<ExerciseRef>>

    /** Strength volume for one exercise, one point per session, oldest → newest. */
    @Query(
        """
        SELECT s.startedAt AS startedAt, COALESCE(SUM(se.reps * se.loadKg), 0) AS volume
        FROM set_entries se
        INNER JOIN logged_items li ON se.loggedItemId = li.id
        INNER JOIN sessions s ON li.sessionId = s.id
        WHERE li.exerciseId = :exerciseId
          AND se.reps IS NOT NULL AND se.loadKg IS NOT NULL
          AND s.deleted = 0
        GROUP BY s.id
        ORDER BY s.startedAt ASC
        """,
    )
    fun volumeOverTime(exerciseId: String): Flow<List<VolumePoint>>
}
