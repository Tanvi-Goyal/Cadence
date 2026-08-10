package com.mindset.model

/**
 * Which capture fields a set shows *by default*. A [SetEntry] still holds every metric as a nullable
 * value; this only drives the Live Logging row's inputs. See [CaptureFields].
 */
enum class MetricType {
    WEIGHT_REPS,
    REPS_ONLY,
    DISTANCE_TIME,
    DURATION,
    CALORIES,
}
