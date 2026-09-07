package com.mindset.data.local

import com.mindset.model.HyroxStation
import com.mindset.model.MetricType
import com.mindset.model.Modality
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
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
        val (modality, defaultMetric) = classify()
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
            // Legacy 2-value column, kept for the existing index; `defaultMetric` is authoritative now.
            metric = if (category == "cardio") ExerciseMetric.TIME_DISTANCE else ExerciseMetric.WEIGHT_REPS,
            force = force,
            level = level,
            mechanic = mechanic,
            equipment = equipment,
            primaryMuscles = json.encodeToString(primaryMuscles),
            secondaryMuscles = json.encodeToString(secondaryMuscles),
            instructions = json.encodeToString(instructions),
            imageUrls = json.encodeToString(images.map { IMAGE_BASE + it }),
            keywords = keywords,
            modality = modality.name,
            defaultMetric = defaultMetric.name,
            hyroxStation = null,
        )
    }

    private fun ExerciseDto.classify(): Pair<Modality, MetricType> {
        OVERRIDES[id]?.let { return it }
        val modality = when {
            category == "cardio" && isRun() -> Modality.RUN
            category == "cardio" -> Modality.CONDITIONING
            category == "plyometrics" -> Modality.CONDITIONING
            category == "stretching" -> Modality.MOBILITY
            else -> Modality.STRENGTH // strength, powerlifting, strongman, olympic weightlifting
        }
        val metric = when {
            category == "cardio" -> MetricType.DISTANCE_TIME
            category == "stretching" -> MetricType.DURATION
            category == "plyometrics" -> MetricType.REPS_ONLY
            equipment == "body only" -> MetricType.REPS_ONLY
            else -> MetricType.WEIGHT_REPS
        }
        return modality to metric
    }

    private fun ExerciseDto.isRun(): Boolean = name.lowercase().let { "run" in it || "sprint" in it || "jog" in it }

    private val OVERRIDES: Map<String, Pair<Modality, MetricType>> = emptyMap()

    /**
     * Curated rows the public dataset doesn't carry: the 8 Hyrox stations (tagged with
     * [Exercise.hyroxStation]) plus calorie-scored conditioning machines. Stable slug ids so a
     * station logged on one device resolves to the same row on another. Rep/weight *standards* stay
     * out of here — those are Phase-3 `RaceFormat` data (ROADMAP decision #4).
     */
    fun supplementalSeed(): List<Exercise> = HyroxStationHelper().hyroxStations + HyroxStationHelper().hyroxRun

    fun seedRow(
        id: String,
        name: String,
        category: String,
        modality: Modality,
        metric: MetricType,
        equipment: String,
        station: HyroxStation?,
        muscles: List<String> = emptyList(),
    ): Exercise = Exercise(
        id = id,
        name = name,
        category = category,
        metric = if (metric == MetricType.WEIGHT_REPS) ExerciseMetric.WEIGHT_REPS else ExerciseMetric.TIME_DISTANCE,
        equipment = equipment,
        primaryMuscles = if (muscles.isEmpty()) "" else json.encodeToString(muscles),
        secondaryMuscles = "",
        instructions = "",
        imageUrls = "",
        keywords = "$name $category $equipment ${muscles.joinToString(" ")}".lowercase(),
        modality = modality.name,
        defaultMetric = metric.name,
        hyroxStation = station?.name,
    )
}
