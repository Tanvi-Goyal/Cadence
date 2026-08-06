package dev.cadence.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.cadence.data.local.PersonalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: PersonalRecord)

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    suspend fun getForExercise(exerciseId: String): List<PersonalRecord>
}
