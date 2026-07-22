package dev.cadence.domain

/**
 * Stable filter options for the exercise picker chips, from the free-exercise-db taxonomy. Hardcoded
 * (not queried) because they're fixed dataset facts and this avoids a `List<String>` DAO return.
 */
object ExerciseFilters {
    val muscles: List<String> = listOf(
        "abdominals", "abductors", "adductors", "biceps", "calves", "chest", "forearms",
        "glutes", "hamstrings", "lats", "lower back", "middle back", "neck", "quadriceps",
        "shoulders", "traps", "triceps",
    )

    val equipment: List<String> = listOf(
        "bands", "barbell", "body only", "cable", "dumbbell", "e-z curl bar", "exercise ball",
        "foam roll", "kettlebells", "machine", "medicine ball", "other",
    )
}
