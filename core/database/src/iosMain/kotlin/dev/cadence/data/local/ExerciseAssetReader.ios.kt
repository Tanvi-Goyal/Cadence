package dev.cadence.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/** Reads `exercises.json` from the iOS app bundle. */
@OptIn(ExperimentalForeignApi::class)
fun iosExerciseAssetReader(): ExerciseAssetReader =
    object : ExerciseAssetReader {
        override fun readExercisesJson(): String {
            val path = NSBundle.mainBundle.pathForResource("exercises", ofType = "json")
                ?: error("exercises.json missing from app bundle")
            return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
                ?: error("could not read exercises.json")
        }
    }
