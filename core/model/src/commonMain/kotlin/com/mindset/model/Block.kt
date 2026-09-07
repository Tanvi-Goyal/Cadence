@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class BlockType { STRAIGHT, INTERVAL, RUN }

enum class BlockSection { WARMUP, MAIN, ACCESSORY, CONDITIONING, CORE }

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
    val capSeconds: Long? = null,
    val workSeconds: Long? = null,
    val section: BlockSection? = null,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
