package dev.cadence.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Parsing + mapping of the free-exercise-db shape into [Exercise] rows. */
class ExerciseImporterTest {

    private val sample = """
    [
      {
        "id": "Barbell_Bench_Press",
        "name": "Barbell Bench Press",
        "force": "push",
        "level": "beginner",
        "mechanic": "compound",
        "equipment": "barbell",
        "primaryMuscles": ["chest"],
        "secondaryMuscles": ["shoulders", "triceps"],
        "instructions": ["Lie on the bench.", "Press up."],
        "category": "strength",
        "images": ["Barbell_Bench_Press/0.jpg", "Barbell_Bench_Press/1.jpg"]
      },
      {
        "id": "Running",
        "name": "Running",
        "equipment": null,
        "primaryMuscles": ["quadriceps"],
        "secondaryMuscles": [],
        "instructions": ["Run."],
        "category": "cardio",
        "images": []
      }
    ]
    """.trimIndent()

    @Test
    fun parse_mapsFieldsAndDerivesMetric() {
        val result = ExerciseImporter.parse(sample)
        assertEquals(2, result.size)

        val bench = result.first { it.id == "Barbell_Bench_Press" }
        assertEquals("Barbell Bench Press", bench.name)
        assertEquals(ExerciseMetric.WEIGHT_REPS, bench.metric, "strength → WEIGHT_REPS")
        assertEquals("barbell", bench.equipment)
        assertEquals(listOf("chest"), bench.primaryMusclesList)
        assertEquals(listOf("shoulders", "triceps"), bench.secondaryMusclesList)
        assertEquals(2, bench.instructionsList.size)
        assertTrue(bench.imageUrlsList.first().startsWith("https://raw.githubusercontent.com/"))
        assertTrue(bench.imageUrlsList.first().endsWith("Barbell_Bench_Press/0.jpg"))
        assertTrue("chest" in bench.keywords && "barbell" in bench.keywords, "keywords are searchable")

        val run = result.first { it.id == "Running" }
        assertEquals(ExerciseMetric.TIME_DISTANCE, run.metric, "cardio → TIME_DISTANCE")
        assertTrue(run.imageUrlsList.isEmpty())
    }
}
