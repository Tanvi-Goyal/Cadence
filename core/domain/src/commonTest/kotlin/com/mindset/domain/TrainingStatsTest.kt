package com.mindset.domain

import kotlin.test.Test
import kotlin.test.assertEquals

private const val DAY = 86_400_000L

/** Epoch-day 19_723 is 2024-01-01, a Monday — a stable anchor for the week-window math. */
private const val MONDAY = 19_723L

class TrainingStatsTest {
    @Test
    fun longestStreak_emptyIsZero() {
        assertEquals(0, longestStreakDays(emptySet()))
    }

    @Test
    fun longestStreak_singleDayIsOne() {
        assertEquals(1, longestStreakDays(setOf(20_000L)))
    }

    @Test
    fun longestStreak_countsConsecutiveRun() {
        assertEquals(3, longestStreakDays(setOf(10L, 11L, 12L)))
    }

    @Test
    fun longestStreak_picksTheLongestRunAcrossGaps() {
        // Two runs: {1,2} (len 2) and {5,6,7,8} (len 4); order-independent.
        assertEquals(4, longestStreakDays(setOf(6L, 2L, 8L, 1L, 5L, 7L)))
    }

    @Test
    fun weeklyCounts_emptyHistoryIsAllZeroes() {
        val buckets = weeklySessionCounts(emptyList(), MONDAY * DAY, weeks = 8)
        assertEquals(8, buckets.size)
        assertEquals(List(8) { 0 }, buckets.map { it.sessionCount })
    }

    @Test
    fun weeklyCounts_marksOnlyTheLastBucketCurrent() {
        val buckets = weeklySessionCounts(emptyList(), MONDAY * DAY, weeks = 8)
        assertEquals(listOf(7), buckets.indices.filter { buckets[it].isCurrent })
    }

    @Test
    fun weeklyCounts_bucketsAreContiguousMondaysOldestFirst() {
        val buckets = weeklySessionCounts(emptyList(), MONDAY * DAY, weeks = 8)
        assertEquals(MONDAY - 49L, buckets.first().mondayEpochDay)
        assertEquals(MONDAY, buckets.last().mondayEpochDay)
        assertEquals(List(8) { MONDAY - 49L + it * 7L }, buckets.map { it.mondayEpochDay })
    }

    @Test
    fun weeklyCounts_countsSessionsNotDistinctDays() {
        // Two workouts on one day are two sessions — the deliberate difference from Home's day grid.
        val sameDay = listOf(MONDAY * DAY, MONDAY * DAY + 6 * 3_600_000L)
        assertEquals(2, weeklySessionCounts(sameDay, MONDAY * DAY, weeks = 8).last().sessionCount)
    }

    @Test
    fun weeklyCounts_sundayBelongsToTheWeekItsMondayOpened() {
        // The Sunday of the previous week (its Monday is MONDAY - 7) → bucket 6, not bucket 7.
        val previousSunday = (MONDAY - 1L) * DAY
        val buckets = weeklySessionCounts(listOf(previousSunday), MONDAY * DAY, weeks = 8)
        assertEquals(1, buckets[6].sessionCount)
        assertEquals(0, buckets[7].sessionCount)
    }

    @Test
    fun weeklyCounts_ignoresSessionsOutsideTheWindow() {
        // One day before the oldest bucket's Monday, and one a full week after the current one.
        val outside = listOf((MONDAY - 50L) * DAY, (MONDAY + 7L) * DAY)
        val buckets = weeklySessionCounts(outside, MONDAY * DAY, weeks = 8)
        assertEquals(0, buckets.sumOf { it.sessionCount })
    }

    @Test
    fun weeklyCounts_midWeekNowStillAnchorsOnMonday() {
        // now = Thursday afternoon; the current bucket is still that Monday's week.
        val thursday = (MONDAY + 3L) * DAY + 15 * 3_600_000L
        val buckets = weeklySessionCounts(listOf(MONDAY * DAY), thursday, weeks = 8)
        assertEquals(MONDAY, buckets.last().mondayEpochDay)
        assertEquals(1, buckets.last().sessionCount)
    }
}
