package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * A cached personal record (new in v8). Cached for a fast PB screen + completion toast but always
 * recomputable from history. `kind` is `dev.cadence.model.PrKind.name`; `value` is interpreted per
 * kind (e1RM kg / weight kg / reps / seconds / calories); `distanceBucketM` is set only for
 * BEST_TIME. Indexed for the "current PR of this kind (and bucket)" lookup.
 *
 * NOTE (A3): added additively; PB detection writes these rows in A5.
 */
@Entity(
    tableName = "personal_records",
    indices = [Index("exerciseId"), Index("exerciseId", "kind", "distanceBucketM")],
)
data class PersonalRecord(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val kind: String,
    val value: Double,
    val distanceBucketM: Int? = null,
    val achievedAt: Long,
    val sourceSetId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Dao
interface PersonalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: PersonalRecord)

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    fun observeForExercise(exerciseId: String): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    suspend fun getForExercise(exerciseId: String): List<PersonalRecord>
}
