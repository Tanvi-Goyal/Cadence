package com.mindset.domain

import com.mindset.model.MetricType
import com.mindset.model.SessionDetail
import com.mindset.model.SessionType

/**
 * Derive a [SessionType] from what a session actually contains, so the Log Session screen never has
 * to ask the user to pick one. Rules, in priority order:
 * 1. Any entry tagged with a `segmentKey` is a Hyrox station → the whole session is [SessionType.HYROX].
 * 2. Otherwise, if every exercise is strength-shaped ([MetricType.WEIGHT_REPS]/[MetricType.REPS_ONLY])
 *    → [SessionType.STRENGTH]; if every exercise is conditioning-shaped (distance/duration/calories)
 *    → [SessionType.CONDITIONING]; a mix of the two → [SessionType.MIXED].
 * 3. An empty session defaults to [SessionType.STRENGTH].
 *
 * Pure and framework-free so it drives both the live header tag and the value persisted on Complete,
 * and is unit-testable in commonTest.
 */
fun deriveSessionType(detail: SessionDetail?): SessionType {
    val entries = detail?.blocks?.flatMap { it.entries }.orEmpty()
    if (entries.isEmpty()) return SessionType.STRENGTH
    if (entries.any { it.entry.segmentKey != null }) return SessionType.HYROX

    val metrics = entries.map { it.exercise.defaultMetric }
    val allStrength = metrics.all { it == MetricType.WEIGHT_REPS || it == MetricType.REPS_ONLY }
    if (allStrength) return SessionType.STRENGTH

    val allConditioning =
        metrics.all {
            it == MetricType.DISTANCE_TIME || it == MetricType.REPS_TIME ||
                it == MetricType.DURATION || it == MetricType.CALORIES
        }
    if (allConditioning) return SessionType.CONDITIONING

    return SessionType.MIXED
}
