package dev.cadence.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.cadence.data.local.AthleteProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AthleteProfileDao {
    @Query("SELECT * FROM athlete_profile WHERE id = 0")
    fun observe(): Flow<AthleteProfileEntity?>

    @Upsert
    suspend fun upsert(profile: AthleteProfileEntity)
}