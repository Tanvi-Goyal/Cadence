package dev.cadence.data.local.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import dev.cadence.data.local.Exercise

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface ExerciseDao {
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>)

    /** Insert-or-update — used by the versioned re-seed to refresh reference rows in place. */
    @Upsert
    suspend fun upsertAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): Exercise?

    /** Resolve a specific set of catalog rows — used to hydrate a session's exercises efficiently. */
    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<Exercise>

    @Query("SELECT * FROM exercises ORDER BY name")
    suspend fun getAll(): List<Exercise>

    /**
     * Paged, filtered library feed. Free-text matches the denormalized [Exercise.keywords]; the
     * optional [equipment]/[muscle] narrow by exact equipment and by primary-muscle membership.
     * All filters are null-guarded so one query serves the full list, search, and chip filtering.
     * Room 3.0 turns the [PagingSource] return into a cross-platform DAO.
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE keywords LIKE '%' || :query || '%'
          AND (:equipment IS NULL OR equipment = :equipment)
          AND (:muscle IS NULL OR primaryMuscles LIKE '%"' || :muscle || '"%')
        ORDER BY name
        """,
    )
    fun search(query: String, equipment: String?, muscle: String?): PagingSource<Int, Exercise>
}
