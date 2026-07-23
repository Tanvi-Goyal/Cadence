package dev.cadence.data.local

import android.content.Context

/**
 * Reads `assets/exercises.json` from the merged APK asset namespace. The file itself lives in the
 * app module (`composeApp/src/main/assets/`), NOT here: AGP 9's `com.android.kotlin.multiplatform.library`
 * plugin does not package `androidMain/assets` into the AAR (JetBrains CMP-9547), so a KMP library's
 * assets never reach the APK. `context.assets` resolves against the merged app assets regardless of
 * which module supplied the file, so the reader stays here with the seeder while the file ships in the app.
 */
fun androidExerciseAssetReader(context: Context): ExerciseAssetReader =
    object : ExerciseAssetReader {
        override fun readExercisesJson(): String =
            context.assets.open("exercises.json").bufferedReader().use { it.readText() }
    }
