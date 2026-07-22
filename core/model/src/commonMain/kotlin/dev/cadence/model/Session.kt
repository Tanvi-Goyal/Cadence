@file:OptIn(ExperimentalTime::class)

package dev.cadence.model

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
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant?,
) : Syncable
