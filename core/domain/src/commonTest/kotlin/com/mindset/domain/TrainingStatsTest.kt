package com.mindset.domain

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
