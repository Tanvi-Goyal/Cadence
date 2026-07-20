package dev.cadence.common

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * UUIDv7 generator (RFC 9562 §5.7): a 48-bit big-endian Unix-millisecond timestamp in the high
 * bits, so ids are coarsely time-ordered.
 *
 * Why v7 over the stdlib's v4 (`Uuid.random()`): time-ordered ids append near the right edge of a
 * B-tree primary-key index instead of scattering across leaf pages — far fewer page splits on
 * insert, and creation-order sorting for free. Both matter as the sync change-log and per-user
 * tables grow. Uniqueness still comes from the 74 random bits.
 *
 * Layout (128 bits):
 * `unix_ts_ms[48] | version=0b0111[4] | rand_a[12] | variant=0b10[2] | rand_b[62]`.
 *
 * [clock] and [random] are constructor-injected so generation is fully deterministic under test.
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class UuidV7Generator(
    private val clock: Clock,
    private val random: Random = Random.Default,
) : UuidGenerator {

    override fun newId(): String {
        val tsMs = clock.now().toEpochMilliseconds() and TIMESTAMP_MASK // low 48 bits
        val randA = random.nextInt(1 shl 12).toLong()                   // 12 bits of rand_a
        val msb = (tsMs shl 16) or (VERSION_7 shl 12) or randA
        val lsb = (random.nextLong() and RAND_B_MASK) or VARIANT_RFC4122
        return Uuid.fromLongs(msb, lsb).toString()
    }

    private companion object {
        const val TIMESTAMP_MASK = 0xFFFF_FFFF_FFFFL      // low 48 bits
        const val VERSION_7 = 0x7L                        // 0b0111, lands in bits 15..12 of msb
        const val RAND_B_MASK = 0x3FFF_FFFF_FFFF_FFFFL    // clears the top 2 bits for the variant
        val VARIANT_RFC4122 = 0x2L shl 62                 // 0b10 in the top 2 bits of lsb
    }
}
