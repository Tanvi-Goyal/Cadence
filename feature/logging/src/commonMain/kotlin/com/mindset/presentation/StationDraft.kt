package com.mindset.presentation

/**
 * In-progress capture for one station set, owned by [LogWorkoutViewModel] (keyed by set id). Holds the
 * raw editable text — all station fields are digit-only, so [timeDigits] is a clock buffer ("305" →
 * 3:05) and the rest are plain digit strings; [load] is in the user's display unit. Seeded from the
 * set's actual-or-target; committed to the DB on ✓ (one) or Complete (all).
 */
data class StationDraft(
    val timeDigits: String = "",
    val reps: String = "",
    val load: String = "",
    val dist: String = "",
)
