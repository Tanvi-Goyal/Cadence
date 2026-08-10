package com.mindset.model

sealed interface CaptureFields {
    data object WeightReps : CaptureFields

    data object RepsOnly : CaptureFields

    data object DistanceTime : CaptureFields

    data object Duration : CaptureFields

    data object Calories : CaptureFields

    companion object {
        fun of(metric: MetricType): CaptureFields = when (metric) {
            MetricType.WEIGHT_REPS -> WeightReps
            MetricType.REPS_ONLY -> RepsOnly
            MetricType.DISTANCE_TIME -> DistanceTime
            MetricType.DURATION -> Duration
            MetricType.CALORIES -> Calories
        }
    }
}
