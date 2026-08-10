package com.mindset.model

/**
 * A supported competition/event format. Iteration-3 generalization of the previously Hyrox-specific
 * reference data: Hyrox is the v1 seeded instance, and DEKA / CrossFit arrive later as pure seed rows
 * (new [EventFormat] + [EventSegment]/[EventDivision]/[SegmentStandard] rows) with **no migration**.
 *
 * Reference data — identical seeded rows on every device, referenced by stable-slug key, never synced.
 */
data class EventFormat(val formatKey: String, val name: String, val description: String = "") {
    companion object {
        /** The v1 event. Used as the [formatKey] on [RaceGoal]s and race [Session]s. */
        const val HYROX = "HYROX"
    }
}
