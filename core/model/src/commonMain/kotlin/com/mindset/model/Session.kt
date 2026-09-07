@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The session's dominant modality mix, used for badges/filtering. */
enum class SessionType { STRENGTH, CONDITIONING, HYROX, MIXED }

/** How a session came to exist. RACE_SIM / HEALTH_CONNECT are reserved for later phases. */
enum class SessionSource { MANUAL, FROM_TEMPLATE, RACE_SIM, HEALTH_CONNECT }

/**
 * A training session — the aggregate root the app logs. A **template** is just a session with
 * [isTemplate] = true whose sets carry only targets; instantiating one deep-copies the tree into a
 * fresh session (snapshot independence). [templateId] records provenance for adherence reporting.
 */
data class Session(
    override val id: String,
    val startedAt: Instant,
    val name: String,
    val type: SessionType,
    val notes: String?,
    val isTemplate: Boolean,
    val source: SessionSource,
    val templateId: String?,
    val category: String? = null,
    val focus: String? = null,
    val programWeek: Int? = null,
    val finishedAt: Instant? = null,
    val raceGoalId: String? = null,
    val formatKey: String? = null,
    val divisionKey: String? = null,
    val avgHeartRate: Int? = null,
    val caloriesKcal: Int? = null,
    val perceivedEffort: Int? = null,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
