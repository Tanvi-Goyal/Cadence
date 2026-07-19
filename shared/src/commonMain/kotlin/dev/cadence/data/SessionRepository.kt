package dev.cadence.data

import androidx.paging.PagingData
import dev.cadence.data.local.Exercise
import dev.cadence.data.local.ExerciseRef
import dev.cadence.data.local.LoggedItemWithSets
import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import dev.cadence.data.local.SetEntry
import dev.cadence.data.local.VolumePoint
import kotlinx.coroutines.flow.Flow

/**
 * The app's entry point to session data. Deliberately framework-agnostic (plain interface, plain
 * types) — nothing here knows about Koin, so the DI framework can be swapped without touching it.
 */
interface SessionRepository {

    /** The single source of truth for the UI: a reactive stream of non-deleted, non-template sessions. */
    fun observeSessions(): Flow<List<Session>>

    /** Reactive stream of the user's templates (D2) — for the template picker. */
    fun observeTemplates(): Flow<List<Session>>

    /** The current planned/next session shown on the Home "Today" card (null if none). */
    fun observePlannedSession(): Flow<PlannedSession?>

    /** One session, reactive — for the Log Workout header. */
    fun observeSession(sessionId: String): Flow<Session?>

    /** A session's logged exercises with their sets, reactive — what Log Workout renders. */
    fun observeLoggedItems(sessionId: String): Flow<List<LoggedItemWithSets>>

    /** Strength volume (Σ reps×loadKg) keyed by session id, reactive — for Home stats/rows. */
    fun observeVolumesBySession(): Flow<Map<String, Double>>

    /** Exercises that have logged data — for the Stats chart selector. */
    fun observeExercisesWithHistory(): Flow<List<ExerciseRef>>

    /** Per-exercise volume over time (oldest→newest) — the Stats trend chart. */
    fun observeVolumeOverTime(exerciseId: String): Flow<List<VolumePoint>>

    /**
     * Paged exercise library. Empty [query] = all; [query] free-text matches name/muscle/equipment;
     * [equipment] and [muscle] narrow by exact equipment and primary-muscle membership. Backed by a
     * Room [PagingSource].
     */
    fun searchExercises(
        query: String,
        equipment: String? = null,
        muscle: String? = null,
    ): Flow<PagingData<Exercise>>

    /** The full seeded catalog as a lookup, for resolving `exerciseId` → name/metric in the UI. */
    suspend fun exercisesById(): Map<String, Exercise>

    /** A single catalog exercise by id (for the Exercise Detail screen). */
    suspend fun exerciseById(id: String): Exercise?

    /** Creates a blank session, persisting it and enqueuing its sync mutation atomically. */
    suspend fun createSession(type: String): Session

    /**
     * Creates an empty template (a session with `isTemplate = true`), atomically with its outbox
     * row. Build it up with [addExercise] + [addTargetSet], then spawn sessions via
     * [instantiateTemplate].
     */
    suspend fun createTemplate(name: String, type: String): Session

    /** Starts a real session from a plan (carries its name/type), atomically with its outbox row. */
    suspend fun startPlannedSession(plan: PlannedSession): Session

    /** Adds an exercise to a session (a new [dev.cadence.data.local.LoggedItem]). */
    suspend fun addExercise(sessionId: String, exerciseId: String)

    /** Appends a set to a logged item. Strength uses reps/loadKg; conditioning uses timeSec/distanceM. */
    suspend fun addSet(
        sessionId: String,
        loggedItemId: String,
        reps: Int? = null,
        loadKg: Double? = null,
        timeSec: Int? = null,
        distanceM: Int? = null,
    )

    /**
     * Appends a **prescription** set to a logged item — writes the `target*` columns, leaving
     * actuals null. Used when building a template. Mirrors [addSet] and touches the parent session.
     */
    suspend fun addTargetSet(
        sessionId: String,
        loggedItemId: String,
        reps: Int? = null,
        loadKg: Double? = null,
        timeSec: Int? = null,
        distanceM: Int? = null,
    )

    /**
     * Persists an edited [set] (typically actuals filled in against a ghost target) and touches the
     * parent [sessionId] so the whole aggregate re-syncs — both in one transaction. The caller owns
     * the copy: pass `set.copy(reps = …, loadKg = …)` with the new values.
     */
    suspend fun updateSet(sessionId: String, set: SetEntry)

    /**
     * Instantiates a template into a new, real session (D2's core flow). Deep-copies the whole tree:
     * each set's targets are copied and its actuals left null (so the UI shows ghost values), the new
     * session records [Session.templateId] provenance with `source = FROM_TEMPLATE`. Copy-on-
     * instantiate — later edits to the template never touch sessions already spawned from it.
     */
    suspend fun instantiateTemplate(templateId: String): Session

    /** Inserts a sensible default plan + the exercise catalog if absent. */
    suspend fun ensureSeeded()

    /**
     * Benchmark-only: ensure at least [target] logged sessions exist (with a few sets each) so the
     * History scroll benchmark has a non-trivial list. Idempotent — a no-op once the count is met.
     */
    suspend fun seedBenchmarkSessions(target: Int)
}
