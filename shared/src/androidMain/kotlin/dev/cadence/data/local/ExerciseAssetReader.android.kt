package dev.cadence.data.local

import android.content.Context

/** Reads `assets/exercises.json` from the Android app package. */
fun androidExerciseAssetReader(context: Context): ExerciseAssetReader =
    object : ExerciseAssetReader {
        override fun readExercisesJson(): String =
            context.assets.open("exercises.json").bufferedReader().use { it.readText() }
    }
