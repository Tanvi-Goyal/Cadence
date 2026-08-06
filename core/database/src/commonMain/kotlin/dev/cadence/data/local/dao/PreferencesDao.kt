package dev.cadence.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.cadence.data.local.PreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM preferences WHERE id = 0")
    fun observe(): Flow<PreferencesEntity?>

    @Upsert
    suspend fun upsert(preferences: PreferencesEntity)
}
