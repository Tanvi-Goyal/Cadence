package com.mindset.domain

import com.mindset.model.CaptureFields
import com.mindset.model.SetEntry

private const val DASH = "—"

/**
 * A read-only, metric-aware one-line summary of a set's **actuals** for the history/detail screen —
 * e.g. Run `"1000 m · 4:32"`, Wall Balls `"100 reps · 3:15"`, Strength `"5 × 100 kg"`. Any value that
 * was never logged renders as an em-dash ([DASH]) rather than a misleading `0` or the planned target.
 * Pure + unit-testable; the caller supplies the display [unit] (kept a UI concern, as elsewhere).
 */
fun SetEntry.detailSummary(capture: CaptureFields, unit: WeightUnit): String = when (capture) {
    CaptureFields.WeightReps -> "${reps?.toString() ?: DASH} × ${loadText(loadKg, unit)}"
    CaptureFields.RepsOnly -> "${reps?.toString() ?: DASH} reps"
    CaptureFields.DistanceTime -> "${distanceM?.let { "$it m" } ?: DASH} · ${clockOrDash(timeSec)}"
    CaptureFields.RepsTime -> "${reps?.let { "$it reps" } ?: DASH} · ${clockOrDash(timeSec)}"
    CaptureFields.Duration -> clockOrDash(timeSec)
    CaptureFields.Calories -> "${calories?.toString() ?: DASH} cal"
}

private fun clockOrDash(sec: Int?): String = sec?.let { formatClockSec(it.toLong()) } ?: DASH

private fun loadText(kg: Double?, unit: WeightUnit): String {
    if (kg == null) return DASH
    val v = Units.toDisplay(kg, unit)
    val plain = if (v % 1.0 == 0.0) v.toInt().toString() else ((v * 10).toInt() / 10.0).toString()
    return "$plain ${Units.label(unit)}"
}
