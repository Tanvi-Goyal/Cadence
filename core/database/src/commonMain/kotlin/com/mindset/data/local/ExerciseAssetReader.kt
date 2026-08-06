package com.mindset.data.local

/**
 * Reads the bundled `exercises.json` as raw text. Platform seam: Android reads from `assets/`
 * (needs a `Context`), iOS from the app bundle — so the concrete reader is supplied via the
 * platform Koin module, like the Room database builder.
 */
interface ExerciseAssetReader {
    fun readExercisesJson(): String
}
