package com.mindset.model

/**
 * A segment of an [EventFormat] is a RUN leg, a functional STATION, or a TRANSITION (roxzone).
 */
enum class SegmentKind { RUN, STATION, TRANSITION }

/**
 * One ordered segment of an [EventFormat] — a [SegmentKind.RUN] leg, a functional [SegmentKind.STATION],
 * or a [SegmentKind.TRANSITION] (roxzone).
 */
data class EventSegment(
    val id: String,
    val formatKey: String,
    val orderIndex: Int,
    val kind: SegmentKind,
    val exerciseId: String,
    val name: String,
    val label: String = "",
    val metric: MetricType,
    val distanceM: Int? = null,
    val reps: Int? = null,
    val loadType: String? = null,
    val descriptor: String = "",
)
