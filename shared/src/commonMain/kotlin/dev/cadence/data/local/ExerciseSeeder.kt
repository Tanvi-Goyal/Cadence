package dev.cadence.data.local

/**
 * The bundled exercise catalog, seeded on first run. IDs are stable slugs so a `LoggedItem`
 * referencing e.g. "bench-press" resolves to the same exercise on every device (exercises are
 * seeded, not synced). Hyrox-flavored conditioning movements are included to match the app's
 * hybrid-athlete focus.
 */
object ExerciseCatalog {

    private fun strength(id: String, name: String, category: String) =
        Exercise(id, name, category, ExerciseMetric.WEIGHT_REPS)

    private fun cardio(id: String, name: String, category: String = "Conditioning") =
        Exercise(id, name, category, ExerciseMetric.TIME_DISTANCE)

    val all: List<Exercise> = listOf(
        // Chest
        strength("bench-press", "Barbell Bench Press", "Chest"),
        strength("incline-db-press", "Incline Dumbbell Press", "Chest"),
        strength("push-up", "Push-Up", "Chest"),
        strength("cable-fly", "Cable Fly", "Chest"),
        strength("dip", "Dip", "Chest"),
        // Back
        strength("deadlift", "Deadlift", "Back"),
        strength("pull-up", "Pull-Up", "Back"),
        strength("barbell-row", "Barbell Row", "Back"),
        strength("lat-pulldown", "Lat Pulldown", "Back"),
        strength("seated-cable-row", "Seated Cable Row", "Back"),
        strength("face-pull", "Face Pull", "Back"),
        // Legs
        strength("back-squat", "Back Squat", "Legs"),
        strength("front-squat", "Front Squat", "Legs"),
        strength("romanian-deadlift", "Romanian Deadlift", "Legs"),
        strength("leg-press", "Leg Press", "Legs"),
        strength("walking-lunge", "Walking Lunge", "Legs"),
        strength("bulgarian-split-squat", "Bulgarian Split Squat", "Legs"),
        strength("leg-curl", "Leg Curl", "Legs"),
        strength("calf-raise", "Calf Raise", "Legs"),
        // Shoulders
        strength("overhead-press", "Overhead Press", "Shoulders"),
        strength("db-shoulder-press", "Dumbbell Shoulder Press", "Shoulders"),
        strength("lateral-raise", "Lateral Raise", "Shoulders"),
        strength("rear-delt-fly", "Rear Delt Fly", "Shoulders"),
        // Arms
        strength("barbell-curl", "Barbell Curl", "Arms"),
        strength("db-curl", "Dumbbell Curl", "Arms"),
        strength("hammer-curl", "Hammer Curl", "Arms"),
        strength("tricep-pushdown", "Tricep Pushdown", "Arms"),
        strength("skullcrusher", "Skullcrusher", "Arms"),
        // Core
        strength("plank", "Plank", "Core"),
        strength("hanging-leg-raise", "Hanging Leg Raise", "Core"),
        strength("cable-crunch", "Cable Crunch", "Core"),
        strength("russian-twist", "Russian Twist", "Core"),
        // Conditioning / Hyrox
        cardio("run", "Run"),
        cardio("row-erg", "Row (Erg)"),
        cardio("ski-erg", "SkiErg"),
        cardio("assault-bike", "Assault Bike"),
        cardio("bike-erg", "BikeErg"),
        cardio("sled-push", "Sled Push"),
        cardio("sled-pull", "Sled Pull"),
        cardio("burpee-broad-jump", "Burpee Broad Jump"),
        cardio("farmers-carry", "Farmers Carry"),
        cardio("sandbag-lunge", "Sandbag Lunge"),
        cardio("wall-ball", "Wall Ball"),
        cardio("box-jump", "Box Jump"),
        cardio("kettlebell-swing", "Kettlebell Swing"),
        cardio("double-under", "Double Under"),
        cardio("jump-rope", "Jump Rope"),
    )
}
