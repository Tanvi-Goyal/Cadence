package dev.cadence.data.local

import dev.cadence.model.HyroxStation
import dev.cadence.model.MetricType
import dev.cadence.model.Modality
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

    /**
     * Derive first-class modality + capture metric from the free-exercise-db fields. Runs are split
     * out of generic conditioning by name; bodyweight strength captures reps only. [OVERRIDES] pins
     * the handful of rows the category heuristic gets wrong (added as they surface).
     */
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

    private fun ExerciseDto.isRun(): Boolean =
        name.lowercase().let { "run" in it || "sprint" in it || "jog" in it }

    private val OVERRIDES: Map<String, Pair<Modality, MetricType>> = emptyMap()

    /**
     * Curated rows the public dataset doesn't carry: the 8 Hyrox stations (tagged with
     * [Exercise.hyroxStation]) plus calorie-scored conditioning machines. Stable slug ids so a
     * station logged on one device resolves to the same row on another. Rep/weight *standards* stay
     * out of here — those are Phase-3 `RaceFormat` data (ROADMAP decision #4).
     */
    fun supplementalSeed(): List<Exercise> = hyroxStations + conditioningMachines

    private val hyroxStations: List<Exercise> = listOf(
        station("hyrox-ski-erg", "Ski Erg", Modality.CONDITIONING, MetricType.DISTANCE_TIME, HyroxStation.SKI_ERG, "ski erg"),
        station("hyrox-sled-push", "Sled Push", Modality.STRENGTH, MetricType.DISTANCE_TIME, HyroxStation.SLED_PUSH, "sled"),
        station("hyrox-sled-pull", "Sled Pull", Modality.STRENGTH, MetricType.DISTANCE_TIME, HyroxStation.SLED_PULL, "sled rope"),
        station("hyrox-burpee-broad-jump", "Burpee Broad Jump", Modality.CONDITIONING, MetricType.DISTANCE_TIME, HyroxStation.BURPEE_BROAD_JUMP, "body only"),
        station("hyrox-rowing", "Rowing", Modality.CONDITIONING, MetricType.DISTANCE_TIME, HyroxStation.ROWING, "rower"),
        station("hyrox-farmers-carry", "Farmers Carry", Modality.STRENGTH, MetricType.DISTANCE_TIME, HyroxStation.FARMERS_CARRY, "kettlebell"),
        station("hyrox-sandbag-lunges", "Sandbag Lunges", Modality.STRENGTH, MetricType.DISTANCE_TIME, HyroxStation.SANDBAG_LUNGES, "sandbag"),
        station("hyrox-wall-balls", "Wall Balls", Modality.CONDITIONING, MetricType.REPS_ONLY, HyroxStation.WALL_BALLS, "medicine ball"),
    )

    private val conditioningMachines: List<Exercise> = listOf(
        machine("assault-bike", "Assault Bike", "air bike"),
        machine("echo-bike", "Echo Bike", "air bike"),
    )

    private fun station(
        id: String,
        name: String,
        modality: Modality,
        metric: MetricType,
        station: HyroxStation,
        equipment: String,
    ): Exercise = seedRow(id, name, "hyrox", modality, metric, equipment, station)

    private fun machine(id: String, name: String, equipment: String): Exercise =
        seedRow(id, name, "conditioning", Modality.CONDITIONING, MetricType.CALORIES, equipment, station = null)

    private fun seedRow(
        id: String,
        name: String,
        category: String,
        modality: Modality,
        metric: MetricType,
        equipment: String,
        station: HyroxStation?,
    ): Exercise = Exercise(
        id = id,
        name = name,
        category = category,
        metric = if (metric == MetricType.WEIGHT_REPS) ExerciseMetric.WEIGHT_REPS else ExerciseMetric.TIME_DISTANCE,
        equipment = equipment,
        primaryMuscles = "",
        secondaryMuscles = "",
        instructions = "",
        imageUrls = "",
        keywords = "$name $category $equipment".lowercase(),
        modality = modality.name,
        defaultMetric = metric.name,
        hyroxStation = station?.name,
    )
}
