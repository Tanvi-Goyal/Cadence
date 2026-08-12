package com.mindset.model

/**
 * Which capture fields a set shows *by default*. A [SetEntry] still holds every metric as a nullable
 * value; this only drives the Live Logging row's inputs. See [CaptureFields].
 */
enum class MetricType {
    WEIGHT_REPS,
    REPS_ONLY,
    DISTANCE_TIME,

    /**
     * Fixed-rep work scored by completion time (e.g. Hyrox Wall Balls — 100 reps *for time*). The
     * recorded variable is time; reps are the fixed target. Parallel to [DISTANCE_TIME], but the PB
     * bucket is the rep target rather than a distance. Captures reps + time.
     */
    REPS_TIME,
    DURATION,
    CALORIES,
}
