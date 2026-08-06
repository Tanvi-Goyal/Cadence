@file:OptIn(ExperimentalTime::class)

package dev.cadence.domain

import dev.cadence.model.Session
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
