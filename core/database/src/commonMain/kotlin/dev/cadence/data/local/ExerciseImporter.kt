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
    fun supplementalSeed(): List<Exercise> = hyroxStations + conditioningMachines + hyfitExercises

    /**
     * HyFit-class movements the public dataset doesn't carry (mobility drills + functional strength),
     * referenced by the seeded program templates ([TemplateSeed]). Stable slug ids. Dataset rows that
     * DO match are reused by slug in TemplateSeed and are NOT duplicated here — e.g. `Pushups`,
     * `Barbell_Curl`, `Barbell_Glute_Bridge`, `Zercher_Squats`, `EZ-Bar_Skullcrusher`,
     * `Bent-Arm_Dumbbell_Pullover`, `Alternating_Renegade_Row`, `Superman`, `Worlds_Greatest_Stretch`,
     * `90_90_Hamstring`, `Crunches`, `Russian_Twist`, `Flutter_Kicks`, `Triceps_Pushdown`, `Pullups`.
     */
    private val hyfitExercises: List<Exercise> = listOf(
        // Warm-up / mobility
        hyfit("cat-cow-thoracic-opener", "Cat/Cow to Thoracic Opener", "stretching", Modality.MOBILITY, MetricType.REPS_ONLY, "body only", "middle back"),
        hyfit("scap-push-up", "Scapular Push-Up", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "shoulders"),
        hyfit("shoulder-rotation", "Shoulder Rotation", "stretching", Modality.MOBILITY, MetricType.REPS_ONLY, "body only", "shoulders"),
        hyfit("hamstring-scoops", "Hamstring Scoops", "stretching", Modality.MOBILITY, MetricType.REPS_ONLY, "body only", "hamstrings"),
        hyfit("glute-bridge-bw", "Glute Bridge (Bodyweight)", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "glutes"),
        hyfit("dog-and-bone", "Dog & Bone Game", "stretching", Modality.MOBILITY, MetricType.REPS_ONLY, "body only", "shoulders"),
        hyfit("hindu-push-up", "Hindu Push-Up", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "chest", "shoulders"),
        hyfit("adductor-opener", "Adductor Opener", "stretching", Modality.MOBILITY, MetricType.REPS_ONLY, "body only", "adductors"),
        hyfit("reverse-snow-angel", "Reverse Snow Angel", "stretching", Modality.MOBILITY, MetricType.REPS_ONLY, "body only", "shoulders"),
        // Main strength
        hyfit("vertical-jump", "Vertical Jump", "plyometrics", Modality.CONDITIONING, MetricType.REPS_ONLY, "body only", "quadriceps"),
        hyfit("single-leg-box-jump", "Single-Leg Box Jump", "plyometrics", Modality.CONDITIONING, MetricType.REPS_ONLY, "body only", "quadriceps"),
        hyfit("broad-jump", "Broad Jump", "plyometrics", Modality.CONDITIONING, MetricType.REPS_ONLY, "body only", "quadriceps", "glutes"),
        hyfit("db-stiff-legged-deadlift", "DB Stiff-Legged Deadlift", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "hamstrings", "glutes"),
        hyfit("db-glutes-bulgarian-split-squat", "DB Glutes-Biased Bulgarian Split-Squat", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "glutes", "quadriceps"),
        hyfit("db-floor-chest-fly", "DB Floor Chest Fly", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "chest"),
        hyfit("staggered-stance-push-up", "Staggered-Stance Push-Up", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "chest", "triceps"),
        hyfit("db-21-curl", "DB 21 Curl", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "biceps"),
        hyfit("banded-triceps-pushdown", "Banded Triceps Pushdown", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "bands", "triceps"),
        hyfit("kb-half-staggered-squat", "KB Half-Set Staggered Squat", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "kettlebells", "quadriceps", "glutes"),
        hyfit("landmine-single-arm-row", "Landmine Single-Arm Bent-Over Row", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "barbell", "middle back", "lats"),
        hyfit("half-kneeling-sa-bb-shoulder-press", "Half-Kneeling Single-Arm BB Shoulder Press", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "barbell", "shoulders"),
        hyfit("half-kneeling-band-pull", "Half-Kneeling Band Pull", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "bands", "lats"),
        hyfit("db-rear-delt-fly", "DB Rear Delt Fly", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "shoulders"),
        // Accessory / burner
        hyfit("curtsy-lunge-pulses", "Curtsy Lunge Pulses", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "glutes"),
        hyfit("kb-swing", "Kettlebell Swing", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "kettlebells", "glutes", "hamstrings"),
        hyfit("plate-zercher-squat-march", "Plate Zercher Squat March", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "other", "quadriceps"),
        hyfit("single-arm-db-floor-chest-press", "Single-Arm DB Floor Chest Press", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "chest"),
        hyfit("pistol-box-squat", "Pistol Box Squat", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "quadriceps"),
        hyfit("wall-squat-hold", "Wall Squat Hold", "strength", Modality.STRENGTH, MetricType.DURATION, "body only", "quadriceps"),
        hyfit("kb-upright-row", "KB Upright Row", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "kettlebells", "shoulders", "traps"),
        hyfit("db-shrug", "DB Shrug", "strength", Modality.STRENGTH, MetricType.WEIGHT_REPS, "dumbbell", "traps"),
        // Conditioning
        hyfit("v-ups", "V-Ups", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "abdominals"),
        hyfit("db-man-makers", "DB Man Makers", "strength", Modality.CONDITIONING, MetricType.REPS_ONLY, "dumbbell", "chest", "middle back"),
        hyfit("bicycle-crunch", "Bicycle Crunch", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "abdominals"),
        hyfit("plate-hold", "Plate Hold (Grip)", "strength", Modality.STRENGTH, MetricType.DURATION, "other", "forearms"),
        hyfit("devil-press", "Devil Press", "strength", Modality.CONDITIONING, MetricType.REPS_ONLY, "dumbbell", "shoulders", "chest"),
        hyfit("mma-plank", "MMA Plank", "strength", Modality.STRENGTH, MetricType.DURATION, "body only", "abdominals"),
        hyfit("high-knees", "High Knees", "cardio", Modality.CONDITIONING, MetricType.DURATION, "body only", "quadriceps"),
        hyfit("jumping-jacks", "Jumping Jacks", "cardio", Modality.CONDITIONING, MetricType.DURATION, "body only", "shoulders"),
        // Core
        hyfit("hollow-hold", "Hollow Hold", "strength", Modality.STRENGTH, MetricType.DURATION, "body only", "abdominals"),
        hyfit("feet-elevated-calf-raise", "Feet-Elevated Standing Calf Raise", "strength", Modality.STRENGTH, MetricType.REPS_ONLY, "body only", "calves"),
    )

    private fun hyfit(
        id: String,
        name: String,
        category: String,
        modality: Modality,
        metric: MetricType,
        equipment: String,
        vararg muscles: String,
    ): Exercise = seedRow(id, name, category, modality, metric, equipment, station = null, muscles = muscles.toList())

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
