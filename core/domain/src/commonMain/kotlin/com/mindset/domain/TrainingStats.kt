@file:OptIn(ExperimentalTime::class)

package com.mindset.domain

import com.mindset.model.Session
import kotlin.time.ExperimentalTime

private const val DAY_MS = 86_400_000L

/**
 * Consecutive-day training streak: the number of UTC epoch-days with ≥1 session, counting back from
 * today (or yesterday if today has none yet, so an active streak doesn't "break" until a full day is
 * missed). UTC-bucketed for a dependency-free v1 — precise local-timezone bucketing is a noted refinement.
 *
 * Shared by Home (the "Day streak" stat) and History (the streak header) so the two never diverge.
 */
fun trainingStreakDays(sessionStartMillis: List<Long>, nowMillis: Long): Int {
    if (sessionStartMillis.isEmpty()) return 0
    val trainedDays = sessionStartMillis.map { it / DAY_MS }.toSet()
    val today = nowMillis / DAY_MS
    var cursor = if (trainedDays.contains(today)) today else today - 1
    var streak = 0
    while (trainedDays.contains(cursor)) {
        streak++
        cursor--
    }
    return streak
}

/**
 * The longest run of consecutive UTC epoch-days that had ≥1 session — the History (Pro) "best streak"
 * stat. Pure over the already-bucketed [trainedEpochDays] set; each run is counted once from its start
 * (a day with no predecessor in the set). 0 when empty.
 */
fun longestStreakDays(trainedEpochDays: Set<Long>): Int {
    var longest = 0
    for (day in trainedEpochDays) {
        if (day - 1 in trainedEpochDays) continue // not the start of a run
        var length = 1
        var cursor = day + 1
        while (cursor in trainedEpochDays) {
            length++
            cursor++
        }
        if (length > longest) longest = length
    }
    return longest
}

/**
 * Session ids that set a new all-time strength-volume high at the time they happened: walking sessions
 * oldest→newest, a session qualifies when its volume (> 0) strictly exceeds every earlier session's.
 * This is the session-level "PB" — the Home "Recent" tag and the History best-effort spotlight both use
 * it. (A per-exercise 1RM definition would be richer but needs a set-level history scan — deferred.)
 */
fun personalBestSessionIds(sessions: List<Session>, volumes: Map<String, Double>): Set<String> {
    val pbs = mutableSetOf<String>()
    var runningMax = 0.0
    for (session in sessions.sortedBy { it.startedAt.toEpochMilliseconds() }) {
        val volume = volumes[session.id] ?: 0.0
        if (volume > 0.0 && volume > runningMax) {
            pbs += session.id
            runningMax = volume
        }
    }
    return pbs
}

/**
 * One ISO-week bucket of [weeklySessionCounts]: the Monday that opens the week (UTC epoch-day), how
 * many sessions fell inside it, and whether it is the in-progress week.
 */
data class WeekBucket(val mondayEpochDay: Long, val sessionCount: Int, val isCurrent: Boolean)

/** The Monday that opens [epochDay]'s week. Epoch-day 0 (1970-01-01) is a Thursday → Monday index 3. */
private fun mondayOf(epochDay: Long): Long = epochDay - (((epochDay % 7) + 3) % 7)

/**
 * Sessions per ISO week over the last [weeks] weeks, oldest first — the Profile training-frequency
 * chart. Counts *sessions*, not distinct trained days (two workouts in a day are two sessions);
 * that is the deliberate difference from Home's "This week" grid, which counts days.
 *
 * Pure over [nowMillis] rather than reading a clock, so the caller owns the window and the bucketing
 * is unit-testable. UTC-bucketed, matching [trainingStreakDays] and the History calendar. Sessions
 * outside the window are ignored, so the caller may pass an unbounded history.
 */
fun weeklySessionCounts(sessionStartMillis: List<Long>, nowMillis: Long, weeks: Int = 8): List<WeekBucket> {
    require(weeks > 0) { "weeks must be positive" }
    val currentMonday = mondayOf(nowMillis / DAY_MS)
    val oldestMonday = currentMonday - (weeks - 1) * 7L
    val counts = IntArray(weeks)
    for (millis in sessionStartMillis) {
        val monday = mondayOf(millis / DAY_MS)
        if (monday < oldestMonday || monday > currentMonday) continue
        counts[((monday - oldestMonday) / 7L).toInt()]++
    }
    return List(weeks) { index ->
        WeekBucket(
            mondayEpochDay = oldestMonday + index * 7L,
            sessionCount = counts[index],
            isCurrent = index == weeks - 1,
        )
    }
}
