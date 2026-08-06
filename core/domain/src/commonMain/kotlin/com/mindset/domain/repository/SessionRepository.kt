package com.mindset.domain.repository

import androidx.paging.PagingData
import com.mindset.model.Exercise
import com.mindset.model.ExerciseRef
import com.mindset.model.HyroxDivisionInfo
import com.mindset.model.HyroxStepDef
import com.mindset.model.HyroxVariant
import com.mindset.model.PlannedSession
import com.mindset.model.Session
import com.mindset.model.SessionDetail
import com.mindset.model.SessionType
import com.mindset.model.SetEntry
import com.mindset.model.VolumePoint
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

    /**
     * A fully hydrated session (its blocks → exercise entries → sets, with each entry's catalog
     * exercise + derived capture fields resolved), reactive — what Log Workout / Session Detail /
     * the template builder render. Emits null while the session id doesn't resolve.
     */
    fun observeSessionDetail(sessionId: String): Flow<SessionDetail?>

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

    /**
     * The reverse-chronological session feed as a Paging 3 stream — the History (Pro) list. [type]
     * narrows by [SessionType] (null = all live, non-template sessions). Backed by a Room PagingSource
     * so the UI only materializes a window; collected in the UI as `LazyPagingItems`.
     */
    fun pagedSessions(type: SessionType? = null): Flow<PagingData<Session>>

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

    /** Adds an exercise to a session (a new [com.mindset.data.local.LoggedItem]). */
    suspend fun addExercise(sessionId: String, exerciseId: String)

    /**
     * Adds an exercise to a real (non-template) session and **prefills its sets from the last time
     * this exercise was logged** — the app's highest-value capture shortcut. The recreated sets carry
     * only `target*` values (actuals null), so the UI renders them as editable ghosts to confirm (✓)
     * or adjust. With no history it seeds no sets (identical to [addExercise] — the add-set row
     * captures the first set). Distinct from [addExercise] so template building stays unaffected.
     */
    suspend fun addExercisePrefilled(sessionId: String, exerciseId: String)

    /**
     * Adds a Hyrox station (identified by its `event_segment` [segmentKey]) to a session, seeding a
     * ghost **target** set from the station's division standard (weight/reps/distance for
     * [divisionKey]) and tagging the entry with its [segmentKey]. Resolved from the seeded reference
     * tables via [hyroxStations].
     */
    suspend fun addStation(sessionId: String, divisionKey: String, segmentKey: String)

    /**
     * The 8 Hyrox stations for [divisionKey] (division-accurate standards) — the "Stations" section of
     * the add-to-session sheet. Thin filter over [hyroxFormat] (`kind == STATION`).
     */
    suspend fun hyroxStations(divisionKey: String): List<HyroxStepDef>

    /**
     * Per-station reference "standard" labels for the athlete's gender (both tiers) at [mode], keyed by
     * `event_segment.id` — e.g. `"78kg (Pro) / 53kg (Open)"`. Gender is taken from [divisionKey]; both
     * the Open and Pro standards are read so the Log Session card can show the full reference. Falls back
     * to SINGLES standards when [mode] has none seeded. Stations with no meaningful standard are omitted.
     */
    suspend fun stationStandardLabels(divisionKey: String, mode: String): Map<String, String>

    /** Updates a session's free-text [notes], touching it so the change re-syncs. */
    suspend fun updateSessionNotes(sessionId: String, notes: String)

    /**
     * Removes an exercise/station ([entryId]) from a session — soft-deletes (tombstones) the entry and
     * its sets and touches the parent session, all in one transaction, so the removal re-syncs.
     */
    suspend fun removeEntry(sessionId: String, entryId: String)

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

    // ── HYROX live workout ──────────────────────────────────────────────────────────────────────

    /** The division options (Women / Men / …) from the seeded HYROX reference tables. */
    suspend fun hyroxDivisions(): List<HyroxDivisionInfo>

    /**
     * The ordered run→station sequence for a [divisionKey] + [variant], resolved from the reference
     * tables (distances, reps, and division-accurate weights). This is the single source both the
     * detail screen and the live timer consume — replacing the in-code `HyroxStandards`.
     */
    suspend fun hyroxFormat(divisionKey: String, variant: HyroxVariant): List<HyroxStepDef>

    /**
     * Synthesizes a live HYROX session (type = HYROX, one block, one entry+set per step carrying the
     * step's targets; actuals null) and returns its id. Written atomically with its outbox row, so the
     * workout is a real, syncable session from the first tick.
     */
    suspend fun startHyroxSession(divisionKey: String, variant: HyroxVariant, templateId: String): String

    /** Records the elapsed split ([elapsedSec]) for the step at [stepIndex] onto its set. */
    suspend fun recordHyroxSplit(sessionId: String, stepIndex: Int, elapsedSec: Int)

    /**
     * Stamps a session finished (total time = `finishedAt − startedAt`), atomically re-syncing it.
     * When [derivedType] is non-null it is persisted as the session's [Session.type] in the same
     * transaction — the Log Session screen passes the type auto-derived from what was logged (see
     * [deriveSessionType]); the Hyrox timer passes null to leave its HYROX type untouched.
     */
    suspend fun finishSession(sessionId: String, derivedType: SessionType? = null)

    /** Inserts a sensible default plan + the exercise catalog if absent. */
    suspend fun ensureSeeded()

    /**
     * Benchmark-only: ensure at least [target] logged sessions exist (with a few sets each) so the
     * History scroll benchmark has a non-trivial list. Idempotent — a no-op once the count is met.
     */
    suspend fun seedBenchmarkSessions(target: Int)
}
