package com.mindset.race


/**
 * The parts of a race that decide whether the notification needs rebuilding.
 *
 * Elapsed time is deliberately **not a field**: it changes 5x/sec, and the whole point is that
 * structural equality here collapses that stream to genuine changes only. Elapsed is passed to
 * [RaceNotifications.build] separately — read when drawing, never when deciding whether to redraw.
 */
internal data class RaceNotice(
    val stepTitle: String,
    val stepIndex: Int,
    val totalSteps: Int,
    val paused: Boolean,
    val finished: Boolean,
) {
    val isLastStep: Boolean get() = stepIndex >= totalSteps - 1
}
