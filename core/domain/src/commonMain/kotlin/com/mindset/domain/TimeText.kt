package com.mindset.domain

/**
 * Time ↔ text helpers shared by the capture UI and read-only summaries. Kept in commonMain so the
 * Log Workout ViewModel (building sets from a digit buffer) and the detail formatter agree on the
 * exact mm:ss convention.
 */

/** Seconds → the raw digit buffer a clock field edits (e.g. 150 → "230", 45 → "45"). Empty for none. */
fun secondsToDigits(sec: Int?): String {
    if (sec == null || sec <= 0) return ""
    val m = sec / 60
    val s = sec % 60
    return if (m == 0) s.toString() else "$m${s.toString().padStart(2, '0')}"
}

/** A clock digit buffer ("305") → total seconds (185). The last two digits are seconds. */
fun digitsToSeconds(digits: String): Int {
    val d = digits.filter { it.isDigit() }
    if (d.isEmpty()) return 0
    val ss = d.takeLast(2).toInt()
    val mm = d.dropLast(2).ifEmpty { "0" }.toInt()
    return mm * 60 + ss
}

/**
 * **Seconds** → display clock "m:ss" (e.g. 185 → "3:05"). For read-only summaries.
 *
 * Distinct from `com.mindset.components.formatClockMs`, which takes **milliseconds** and zero-pads
 * the minutes for a live clock. Both take a `Long`, so the unit lives in the name.
 */
fun formatClockSec(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
