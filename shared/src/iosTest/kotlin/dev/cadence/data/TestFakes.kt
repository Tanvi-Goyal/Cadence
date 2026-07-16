package dev.cadence.data

import dev.cadence.data.local.ExerciseAssetReader

/** Empty catalog for repository tests that don't exercise the seeded exercise library. */
val emptyExerciseAssetReader = object : ExerciseAssetReader {
    override fun readExercisesJson(): String = "[]"
}

/** Minimal catalog with a stable `bench-press` slug, for tests that log against the catalog. */
val benchPressAssetReader = object : ExerciseAssetReader {
    override fun readExercisesJson(): String = """
        [{"id":"bench-press","name":"Bench Press","category":"strength","equipment":"barbell",
          "primaryMuscles":["chest"],"secondaryMuscles":[],"instructions":[],"images":[]}]
    """.trimIndent()
}
