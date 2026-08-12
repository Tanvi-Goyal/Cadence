package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.mindset.data.local.PersonalRecord
import com.mindset.model.HyroxRecordRow
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: PersonalRecord)

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>>

    /**
     * All cached PBs for the 8 Hyrox stations (joined to the catalog for the station tag + name),
     * backing the Station board. Division-bucketed rows come through as-is; the repository groups by
     * station. Ordered station → division → bucket for a stable read.
     */
    @Query(
        """
        SELECT pr.exerciseId AS exerciseId, e.name AS exerciseName, e.hyroxStation AS hyroxStation,
               pr.kind AS kind, pr.value AS value, pr.distanceBucketM AS distanceBucketM,
               pr.divisionKey AS divisionKey, pr.achievedAt AS achievedAt
        FROM personal_records pr
        INNER JOIN exercises e ON pr.exerciseId = e.id
        WHERE e.hyroxStation IS NOT NULL AND pr.deletedAt IS NULL
        ORDER BY e.hyroxStation, pr.divisionKey, pr.distanceBucketM
        """,
    )
    fun observeHyroxRecords(): Flow<List<HyroxRecordRow>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    suspend fun getForExercise(exerciseId: String): List<PersonalRecord>
}
