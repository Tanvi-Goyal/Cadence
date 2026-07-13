package dev.cadence.data.local

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter

/**
 * A movement in the bundled exercise library. This is seeded REFERENCE data — identical on every
 * device — so it is deliberately NOT synced. `id` is a stable human-readable slug (e.g.
 * "bench-press"), NOT a random UUID: a `LoggedItem.exerciseId` logged on device A must resolve to
 * the same seeded exercise on device B, and only stable ids guarantee that across devices.
 *
 * [metric] decides how sets are measured and how volume is computed.
 */
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val metric: String,
)

/** How an exercise's sets are measured — drives the Log Workout set-row UI and volume calc. */
object ExerciseMetric {
    const val WEIGHT_REPS = "WEIGHT_REPS"     // strength: reps × loadKg
    const val TIME_DISTANCE = "TIME_DISTANCE" // conditioning: timeSec / distanceM
}

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface ExerciseDao {
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): Exercise?

    @Query("SELECT * FROM exercises ORDER BY name")
    suspend fun getAll(): List<Exercise>

    /**
     * Paged, filtered library feed. Room 3.0 turns a [PagingSource] return type into a
     * cross-platform DAO via [PagingSourceDaoReturnTypeConverter]. An empty query matches all
     * (LIKE '%%'), so one query serves both the full list and search.
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): PagingSource<Int, Exercise>
}
