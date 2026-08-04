package dev.cadence.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A training session — the core unit the app logs.
 *
 * Carries the sync-critical columns (`updatedAt`, `deleted`, `syncStatus`) from day one even
 * though the sync engine lands in Phase 2: designing them in now is the difference between
 * "added sync later" and "designed for sync". `id` is a client-generated UUID so retried pushes
 * are idempotent and two devices never collide on an autoincrement key.
 */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val name: String = "Session",
    val type: String = SessionType.STRENGTH,
    val notes: String? = null,
    // Template support (D2): a template is a session with [isTemplate] = true whose sets carry only
    // targets. [templateId] records which template a real session was spawned from (provenance, for
    // plan-adherence later). [startedAt] is meaningless for templates but stays non-null — the flag,
    // not a null timestamp, is what excludes templates from the history list and stats.
    val isTemplate: Boolean = false,
    val source: String = SessionSource.MANUAL,
    val templateId: String? = null,
    // v9: lightweight template/collection metadata (NOT a program engine) — `category` groups a
    // template into a collection (e.g. "HyFit 6-Week Strength"), `focus` is the day focus ("Glutes &
    // Hamstring"), `programWeek` orders days within a block. All null on ordinary logged sessions.
    val category: String? = null,
    val focus: String? = null,
    val programWeek: Int? = null,
    // v10: stamped when a live workout is completed (null = in progress / not applicable). Enables a
    // total-time readout and a "finished" notion without changing the "live from insert" model.
    val finishedAt: Long? = null,
    // v12 (iteration 3): race-awareness + session summary. A race sim links to the goal it trained
    // for ([raceGoalId]) and self-describes its format/division (so it survives the goal being
    // deleted, and per-station PBs know the weight class). Summary metrics are nullable — v1 has no
    // wearable, so Health Connect (Phase 5) fills HR/calories and [perceivedEffort] (session RPE 1–10)
    // is the only intensity source until then. All null on ordinary non-race sessions.
    val raceGoalId: String? = null,
    val formatKey: String? = null,
    val divisionKey: String? = null,
    val avgHeartRate: Int? = null,
    val caloriesKcal: Int? = null,
    val perceivedEffort: Int? = null,
    val updatedAt: Long,
    val syncStatus: String = SyncStatus.PENDING,
    // Sync envelope. [deletedAt] is the tombstone (null = live) — it replaced the pre-v8 boolean
    // `deleted` in v8. `createdAt` defaults only for interim in-code construction; every write path
    // stamps it, and the v7→v8 migration backfills legacy rows from `startedAt`.
    val createdAt: Long = 0L,
    val deletedAt: Long? = null,
)

/** Sync lifecycle of a row. Kept as string constants (not an enum) so it stores as plain TEXT. */
object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}

/** Training modality of a session. String constants (stored as TEXT) drive the Recent-row badge. */
object SessionType {
    const val STRENGTH = "STRENGTH"
    const val CONDITIONING = "CONDITIONING"
    const val HYROX = "HYROX"
    const val MIXED = "MIXED"
}

/**
 * How a session came to exist (D2 §8, the four generation doors). String constants (stored as TEXT).
 * Only [MANUAL] and [FROM_TEMPLATE] are produced today; [RACE_SIM] and [HEALTH_CONNECT] are reserved
 * for later phases so their sessions need no schema change when those pipelines land.
 */
object SessionSource {
    const val MANUAL = "MANUAL"
    const val FROM_TEMPLATE = "FROM_TEMPLATE"
    const val RACE_SIM = "RACE_SIM"
    const val HEALTH_CONNECT = "HEALTH_CONNECT"
}
