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
    val updatedAt: Long,
    val deleted: Boolean = false,
    val syncStatus: String = SyncStatus.PENDING,
    // v8 sync-envelope additions (A3). The domain model uses these; the pre-v8 boolean [deleted]
    // stays until A4 cuts reads over. `createdAt` defaults for interim construction and A6's
    // migration backfills legacy rows; [deletedAt] (tombstone) supersedes [deleted].
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
