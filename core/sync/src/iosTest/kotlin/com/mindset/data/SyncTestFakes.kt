package com.mindset.data

import com.mindset.data.local.ExerciseAssetReader

/**
 * Empty catalog for [com.mindset.sync.SyncEngineTest], which drives the repo directly and never
 * exercises the seeded library. (The richer fakes live in :core:data's own test source set.)
 */
val emptyExerciseAssetReader = object : ExerciseAssetReader {
    override fun readExercisesJson(): String = "[]"
}
