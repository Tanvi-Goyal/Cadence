package dev.cadence.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * The sync outbox. Each row is a pending mutation to push to the backend in Phase 2.
 *
 * Nothing drains this table yet — but rows are enqueued NOW, in the same transaction as the
 * local write (see the repository), so the "write succeeded locally" and "the server will hear
 * about it" facts can never diverge. That atomic-enqueue is the essence of the outbox pattern.
 */
@Entity(tableName = "outbox")
data class OutboxEntry(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val opType: String,
    val payload: String,
    val createdAt: Long,
    val attempts: Int = 0,
)
