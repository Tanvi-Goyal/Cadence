package com.mindset.common

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Verifies the RFC 9562 §5.7 bit layout and the time-ordering property that motivates choosing v7
 * over v4. In the canonical `8-4-4-4-12` string, the version nibble is the first char of group 3
 * (index 14) and the variant is the first char of group 4 (index 19); the first 12 hex chars
 * (groups 1–2, dashes stripped) are the 48-bit big-endian timestamp.
 */
@OptIn(ExperimentalTime::class)
class UuidV7GeneratorTest {
    private class FakeClock(var instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private fun tsPrefix(id: String): String = id.replace("-", "").substring(0, 12)

    @Test
    fun version_and_variant_bits_are_set() {
        val id =
            UuidV7Generator(
                FakeClock(Instant.fromEpochMilliseconds(1_700_000_000_000)),
            ).newId()

        assertEquals('7', id[14], "version nibble must be 7")
        assertTrue(id[19] in "89ab", "variant nibble must be 10xx (8,9,a,b) but was '${id[19]}'")
    }

    @Test
    fun timestamp_prefix_is_stable_for_a_fixed_clock() {
        val clock = FakeClock(Instant.fromEpochMilliseconds(1_700_000_000_000))
        val gen = UuidV7Generator(clock)

        val a = gen.newId()
        val b = gen.newId()

        assertEquals(tsPrefix(a), tsPrefix(b), "same clock time ⇒ same 48-bit timestamp prefix")
        assertNotEquals(a, b, "random bits still make the ids distinct")
    }

    @Test
    fun ids_are_time_ordered_as_the_clock_advances() {
        val clock = FakeClock(Instant.fromEpochMilliseconds(1_700_000_000_000))
        val gen = UuidV7Generator(clock)

        val earlier = gen.newId()
        clock.instant = Instant.fromEpochMilliseconds(1_700_000_001_000)
        val later = gen.newId()

        // Hex is zero-padded big-endian, so lexicographic order matches numeric order.
        assertTrue(tsPrefix(later) > tsPrefix(earlier), "later id must sort after the earlier id")
    }

    @Test
    fun ids_are_unique_across_many_calls_at_the_same_instant() {
        val gen =
            UuidV7Generator(FakeClock(Instant.fromEpochMilliseconds(1_700_000_000_000)), Random(42))
        val ids = List(10_000) { gen.newId() }
        assertEquals(ids.size, ids.toSet().size, "no collisions from the 74 random bits")
    }
}
