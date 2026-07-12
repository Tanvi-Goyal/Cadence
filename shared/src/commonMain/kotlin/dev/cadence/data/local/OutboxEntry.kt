package dev.cadence.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

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

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(entry: OutboxEntry)

    @Query("SELECT COUNT(*) FROM outbox")
    suspend fun count(): Int

    /** All pending mutations, oldest first — the sync engine drains these in order. */
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    suspend fun getAll(): List<OutboxEntry>

    /** Remove entries once their push is confirmed (done in the same txn that marks rows synced). */
    @Query("DELETE FROM outbox WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
