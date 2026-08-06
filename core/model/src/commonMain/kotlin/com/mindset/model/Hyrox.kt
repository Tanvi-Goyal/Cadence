package com.mindset.model

/** Which slice of the HYROX race a simulation covers. FULL = the complete 8 stations. */
enum class HyroxVariant { FULL, FIRST_HALF, SECOND_HALF, HALVED }

/** A step in a HYROX sequence is either a RUN leg or a functional STATION. */
enum class HyroxStepKind { RUN, STATION }

/**
 * One ordered, structured step of a HYROX workout (a run leg or a station), resolved for a chosen
 * division + variant from the seeded reference tables.
 *
 * The display strings ([title]/[detail]/[value]) mirror the detail screen; the `target*` fields are
 * the numeric prescription persisted onto the synthesized session's set so the completed workout is
 * real, queryable data (not just UI text).
 */
data class HyroxStepDef(
    val index: Int,                 // 0-based position in the flattened run→station sequence
    val kind: HyroxStepKind,
    val station: HyroxStation?,     // the station enum (null for runs) — drives the UI glyph
    val exerciseId: String,         // catalog exercise id backing the synthesized entry
    val title: String,              // "Run 1" / "1. SkiErg"
    val detail: String,             // "1000m Distance" / "6kg Ball • 3m Target"
    val value: String,              // "1.0 km" / "1000m" / "152 kg" / "100 reps"
    val targetDistanceM: Int? = null,
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    /** v12: the `event_segment.id` this step resolves from — persisted onto the entry's `segmentKey`. */
    val segmentKey: String? = null,
)

/** A HYROX division option for the selector (from the seeded reference tables). */
data class HyroxDivisionInfo(val key: String, val label: String)
