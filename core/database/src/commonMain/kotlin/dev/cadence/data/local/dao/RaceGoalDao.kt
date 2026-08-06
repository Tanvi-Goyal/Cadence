package dev.cadence.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.cadence.data.local.AthleteProfileEntity
import dev.cadence.data.local.RaceGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceGoalDao {
    /** Next race being trained for: soonest dated UPCOMING goal; open-ended (null date) goals sort last. */
    @Query(
        "SELECT * FROM race_goal WHERE status = 'UPCOMING' AND deletedAt IS NULL " +
                "ORDER BY targetDate IS NULL, targetDate LIMIT 1",
    )
    fun observeUpcoming(): Flow<RaceGoalEntity?>

    @Query("SELECT * FROM race_goal WHERE deletedAt IS NULL ORDER BY targetDate IS NULL, targetDate DESC")
    fun observeAll(): Flow<List<RaceGoalEntity>>

    @Query("SELECT * FROM race_goal WHERE id = :id")
    suspend fun getById(id: String): RaceGoalEntity?

    @Upsert
    suspend fun upsert(goal: RaceGoalEntity)
}