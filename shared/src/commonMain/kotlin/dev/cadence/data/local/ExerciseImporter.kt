package dev.cadence.data.local

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Raw shape of one entry in the bundled free-exercise-db `exercises.json`. */
@Serializable
private data class ExerciseDto(
    val id: String,
    val name: String,
    val force: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val equipment: String? = null,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val category: String,
    val images: List<String> = emptyList(),
)

/**
 * Parses the bundled free-exercise-db JSON into [Exercise] rows, deriving [Exercise.metric] and the
 * denormalized [Exercise.keywords] and turning relative image paths into absolute public-domain URLs.
 */
object ExerciseImporter {
    private const val IMAGE_BASE =
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(rawJson: String): List<Exercise> =
        json.decodeFromString(ListSerializer(ExerciseDto.serializer()), rawJson).map { it.toEntity() }

    private fun ExerciseDto.toEntity(): Exercise {
        val metric = if (category == "cardio") ExerciseMetric.TIME_DISTANCE else ExerciseMetric.WEIGHT_REPS
        val keywords = buildList {
            add(name)
            addAll(primaryMuscles)
            addAll(secondaryMuscles)
            equipment?.let { add(it) }
            add(category)
        }.joinToString(" ").lowercase()
        return Exercise(
            id = id,
            name = name,
            category = category,
            metric = metric,
            force = force,
            level = level,
            mechanic = mechanic,
            equipment = equipment,
            primaryMuscles = json.encodeToString(primaryMuscles),
            secondaryMuscles = json.encodeToString(secondaryMuscles),
            instructions = json.encodeToString(instructions),
            imageUrls = json.encodeToString(images.map { IMAGE_BASE + it }),
            keywords = keywords,
        )
    }
}
