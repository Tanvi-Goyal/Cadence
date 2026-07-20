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
 * One set in the session tree — the single unit of work (D1: one wide, typed row).
 *
 * Every set carries BOTH a prescription and a performance (D2, template-first capture):
 * - **Actuals** ([reps]/[loadKg]/[timeSec]/[distanceM]) — what was performed. Unified across
 *   modalities: strength uses [reps]/[loadKg], conditioning uses [timeSec]/[distanceM]; the rest
 *   are null. Which pair is meaningful is decided by the parent exercise's [Exercise.metric].
 * - **Targets** ([targetReps]/[targetLoadKg]/[targetTimeSec]/[targetDistanceM]) — the prescription.
 *   In a template row (parent session `isTemplate = true`) only the targets are populated; actuals
 *   are null. Instantiating a template deep-copies targets → targets and leaves actuals null, which
 *   is what gives the UI "ghost values" (target shown greyed until the actual is entered).
 *
 * Note the actuals are deliberately left un-prefixed (not `actualReps`) to keep existing read
 * queries, DTOs, and UI untouched; the `target*` prefix marks the prescription.
 */
@Entity(tableName = "set_entries", indices = [Index("loggedItemId")])
data class SetEntry(
    @PrimaryKey val id: String,
    val loggedItemId: String,
    val setNumber: Int,
    // Performance (actuals):
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val rpe: Int? = null,
    // Prescription (targets) — populated in template rows, copied on instantiation:
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val targetTimeSec: Int? = null,
    val targetDistanceM: Int? = null,
    // v8 additions (A3), appended to keep any positional constructors valid. [calories]/
    // [targetCalories] serve erg + Hyrox capture; the envelope makes the set independently syncable.
    // Values are placeholders until A4 wires the clock seam / A6 backfills legacy rows.
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

    /**
     * Strength volume per session, for Home's Volume stat + per-row metric. Reactive.
     * No template filter needed: template sets carry only targets, so their [reps]/[loadKg] actuals
     * are null and the `IS NOT NULL` guards below exclude them by construction.
     */
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
