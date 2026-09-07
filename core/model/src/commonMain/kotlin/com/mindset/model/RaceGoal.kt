@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A race the athlete is training for. First-class and multi-instance (unlike the single target that
 * used to live on the athlete profile) so the north-star question — *am I getting fitter for THIS
 * race?* — is answerable across a training block and across past races: Home reads the next
 * [RaceGoalStatus.UPCOMING] goal for its countdown, and a race-sim [Session] links back via
 * `raceGoalId`.
 *
 * [Syncable] even though v1 has no networking — it carries the full envelope + enqueues an outbox row
 * on write, so Phase-2 sync adopts it with no schema break (mirrors the [Session] discipline).
 * [formatKey]/[divisionKey] reference the seeded [EventFormat]/[EventDivision]; [targetDate] null =
 * an open-ended goal with no fixed race day.
 */
data class RaceGoal(
    override val id: String,
    val formatKey: String,
    val divisionKey: String,
    val mode: RaceMode,
    val targetDate: Instant? = null,
    val city: String? = null,
    val goalTimeSec: Int? = null,
    val status: RaceGoalStatus,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant? = null,
) : Syncable
