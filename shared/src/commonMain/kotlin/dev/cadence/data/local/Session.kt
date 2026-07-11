package dev.cadence.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val notes: String? = null,
    val updatedAt: Long,
    val deleted: Boolean = false,
    val syncStatus: String = SyncStatus.PENDING,
)

/** Sync lifecycle of a row. Kept as string constants (not an enum) so it stores as plain TEXT. */
object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}
