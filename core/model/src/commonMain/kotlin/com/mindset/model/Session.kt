@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A training session — the aggregate root the app logs. A **template** is just a session with
 * [isTemplate] = true whose sets carry only targets; instantiating one deep-copies the tree into a
 * fresh session (snapshot independence). [templateId] records provenance for adherence reporting.
 *
 * Distinct from the Room `Session` entity: [deletedAt] replaces the entity's boolean `deleted`,
 * [type] is a real enum, and the entity-only `syncStatus` is absent here (see [Syncable]).
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
    /** v9: lightweight template metadata — collection, day focus, and week ordering within a block. */
    val category: String? = null,
    val focus: String? = null,
    val programWeek: Int? = null,
    /** v10: when a live workout was completed (null = in progress / not applicable). */
    val finishedAt: Instant? = null,
    /**
     * v12: race-awareness + summary. A race sim links to the [RaceGoal] it trained for ([raceGoalId])
     * and self-describes its event [formatKey] / [divisionKey] (so it survives the goal being deleted
     * and per-station PBs know the weight class). Summary metrics are nullable — [avgHeartRate] /
     * [caloriesKcal] await Health Connect (Phase 5); [perceivedEffort] (session RPE 1–10) is the only
     * intensity source until then. All null on ordinary non-race sessions.
     */
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
