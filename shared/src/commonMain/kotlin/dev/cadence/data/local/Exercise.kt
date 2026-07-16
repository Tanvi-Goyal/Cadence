package dev.cadence.data.local

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * A movement in the bundled exercise library (imported from the public-domain free-exercise-db).
 * Seeded REFERENCE data — identical on every device, so it is deliberately NOT synced (no
 * `updatedAt`/`deleted`/`syncStatus`, no outbox). `id` is a stable slug: a `LoggedItem.exerciseId`
 * logged on device A must resolve to the same seeded exercise on device B.
 *
 * List fields ([primaryMuscles] etc.) persist as JSON via [Converters]. [keywords] is a denormalized
 * lowercased blob (name + muscles + equipment + category) that powers a single `LIKE` search — FTS4
 * was considered but the bundled SQLite driver's FTS support on Kotlin/Native is unverified and ~870
 * rows make `LIKE` ample. [equipment]/[category] are indexed for exact-match filter chips.
 */
@Entity(
    tableName = "exercises",
    indices = [Index("equipment"), Index("category"), Index("metric")],
)
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,          // dataset type: strength / cardio / stretching / ...
    val metric: String,            // derived: TIME_DISTANCE if cardio, else WEIGHT_REPS
    val force: String? = null,     // push / pull / static
    val level: String? = null,     // beginner / intermediate / expert
    val mechanic: String? = null,  // compound / isolation
    val equipment: String? = null,
    val primaryMuscles: String = "",   // JSON array (see *List accessors)
    val secondaryMuscles: String = "",
    val instructions: String = "",
    val imageUrls: String = "",
    val keywords: String = "",
) {
    // List views of the JSON columns. Getter-only (no backing field) so Room ignores them; this is
    // how the UI consumes the rich data. (Room 3's KMP KSP rejects `List<String>` columns + a
    // TypeConverter — MissingType — so the lists are stored as JSON text and parsed here.)
    val primaryMusclesList: List<String> get() = decodeList(primaryMuscles)
    val secondaryMusclesList: List<String> get() = decodeList(secondaryMuscles)
    val instructionsList: List<String> get() = decodeList(instructions)
    val imageUrlsList: List<String> get() = decodeList(imageUrls)
}

private val listJson = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

private fun decodeList(json: String): List<String> =
    if (json.isEmpty()) emptyList() else listJson.decodeFromString(stringListSerializer, json)

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
