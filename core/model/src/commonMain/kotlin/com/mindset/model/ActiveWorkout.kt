package com.mindset.model

data class ActiveWorkout(
    val sessionId: String,
    val divisionKey: String,
    val variant: HyroxVariant,
    val steps: List<HyroxStationModel>,
    val currentIndex: Int,
    val totalElapsedMs: Long,
    val splitElapsedMs: Long,
    val paused: Boolean,
    val finished: Boolean,
) {
    val totalSteps: Int get() = steps.size
    val current: HyroxStationModel? get() = steps.getOrNull(currentIndex)
    val next1: HyroxStationModel? get() = steps.getOrNull(currentIndex + 1)
    val next2: HyroxStationModel? get() = steps.getOrNull(currentIndex + 2)
}

