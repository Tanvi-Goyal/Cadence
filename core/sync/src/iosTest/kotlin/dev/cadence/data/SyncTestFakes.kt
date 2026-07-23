package dev.cadence.data

import dev.cadence.data.local.ExerciseAssetReader

/**
 * Empty catalog for [dev.cadence.sync.SyncEngineTest], which drives the repo directly and never
 * exercises the seeded library. (The richer fakes live in :core:data's own test source set.)
 */
val emptyExerciseAssetReader = object : ExerciseAssetReader {
    override fun readExercisesJson(): String = "[]"
}
