package com.mindset.model

/**
 * The editable input cells a Live Logging row renders — derived from an exercise's [MetricType]
 * rather than branched throughout the UI. One screen serves every modality: a barbell set shows
 * weight+reps, a run shows distance+time, a Hyrox erg shows calories. Rest timer, ghost values,
 * and tap-to-complete are shared across all variants; only these cells differ.
 */
sealed interface CaptureFields {
    /** weight_kg + reps (barbell, dumbbell). */
    data object WeightReps : CaptureFields

    /** reps only (wall balls, bodyweight). */
    data object RepsOnly : CaptureFields

    /** distance_m + duration_ms → pace derived (runs, ergs for distance). */
    data object DistanceTime : CaptureFields

    /** duration_ms (carries/holds for time). */
    data object Duration : CaptureFields

    /** calories (+ optional duration) — erg stations scored on calories. */
    data object Calories : CaptureFields

    companion object {
        /** Total mapping; the exhaustive `when` (no `else`) makes a new [MetricType] a compile error. */
        fun of(metric: MetricType): CaptureFields = when (metric) {
            MetricType.WEIGHT_REPS -> WeightReps
            MetricType.REPS_ONLY -> RepsOnly
            MetricType.DISTANCE_TIME -> DistanceTime
            MetricType.DURATION -> Duration
            MetricType.CALORIES -> Calories
        }
    }
}
