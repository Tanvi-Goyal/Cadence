package dev.cadence.contracts

import kotlinx.serialization.Serializable

/**
 * The wire representation of a session — the sync contract shared by client and server.
 *
 * **Aggregate transport, per-entity envelope.** The session is still the sync unit: its whole block
 * tree travels with it and Last-Write-Wins compares the session's [updatedAt]. But every node now
 * carries its own stable `id` + envelope (`createdAt`/`updatedAt`/`deletedAt`), so children keep
 * their identity across a push/pull round-trip (they are no longer rebuilt with fresh local ids).
 * That makes a later move to per-entity transport a pure server/engine change — no schema break.
 *
 * Absent by design: `syncStatus` (a local "is this pushed yet?" flag, meaningless on the wire).
 * Deletions propagate via the [deletedAt] tombstone (a hard-deleted row can't be synced once gone).
 * Exercises aren't sent — they're seeded reference data on every device, referenced by `exerciseId`.
 */
@Serializable
data class SessionDto(
    val id: String,
    val startedAt: Long,
    val name: String = "Session",
    val type: String = "STRENGTH",
    val notes: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val isTemplate: Boolean = false,
    val source: String = "MANUAL",
    val templateId: String? = null,
    val blocks: List<BlockDto> = emptyList(),
)

@Serializable
data class BlockDto(
    val id: String,
    val type: String = "STRAIGHT",
    val orderIndex: Int = 0,
    val rounds: Int? = null,
    val restBetweenRoundsMs: Long? = null,
    val label: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
    val entries: List<ExerciseEntryDto> = emptyList(),
)

@Serializable
data class ExerciseEntryDto(
    val id: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int? = null,
    val restMs: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
    val sets: List<SetDto> = emptyList(),
)

@Serializable
data class SetDto(
    val id: String,
    val setNumber: Int,
    // Performance (actuals):
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val calories: Int? = null,
    val rpe: Int? = null,
    // Prescription (targets) — populated for template sets, copied on instantiation:
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val targetTimeSec: Int? = null,
    val targetDistanceM: Int? = null,
    val targetCalories: Int? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)
