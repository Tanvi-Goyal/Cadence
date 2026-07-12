package dev.cadence.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * A planned/next session shown on the Home "Today" card. Deliberately LOCAL-ONLY — plans are not
 * part of the sync contract (no DTO, no outbox). Program planning across devices is a v2 concern;
 * for now one seeded default plan gives the card real content without a program builder.
 */
@Entity(tableName = "planned_sessions")
data class PlannedSession(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val targetDurationMin: Int,
    val focus: String,
)

@Dao
interface PlannedSessionDao {
    /** The current plan (at most one for v1), observed by Home. */
    @Query("SELECT * FROM planned_sessions LIMIT 1")
    fun observeCurrent(): Flow<PlannedSession?>

    @Query("SELECT * FROM planned_sessions LIMIT 1")
    suspend fun getCurrent(): PlannedSession?

    @Upsert
    suspend fun upsert(plan: PlannedSession)
}
