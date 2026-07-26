@file:OptIn(ExperimentalTime::class)

package dev.cadence.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A structural grouping of exercises within a [Session] — the level that makes supersets, circuits,
 * and intervals expressible (a flat exercise list cannot say "3 rounds of A+B+C"). Legacy pre-v8
 * sessions migrate to a single implicit [BlockType.STRAIGHT] block, so this level is additive.
 *
 * [rounds]/[restBetweenRoundsMs] are meaningful for circuits/intervals; a straight block is one
 * round. [label] is an optional human tag ("AMRAP", "Warm-up").
 */
data class Block(
    override val id: String,
    val sessionId: String,
    val type: BlockType,
    val orderIndex: Int,
    val rounds: Int?,
    val restBetweenRoundsMs: Long?,
    val label: String?,
    /** v9: workout phase (warm-up/main/…) and conditioning shape. See [BlockSection]/[ConditioningFormat]. */
    val section: BlockSection? = null,
    val conditioningFormat: ConditioningFormat? = null,
    val capSeconds: Long? = null,
    val workSeconds: Long? = null,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
