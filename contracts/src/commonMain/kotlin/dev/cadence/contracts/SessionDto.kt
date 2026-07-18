package dev.cadence.contracts

import kotlinx.serialization.Serializable

/**
 * The wire representation of a session — the sync contract shared by client and server.
 *
 * Note what's absent: `syncStatus`. That's a purely local concern (is this row pushed yet?) and
 * has no meaning on the wire, so it never crosses the network. `updatedAt` DOES cross — it's the
 * clock the Last-Write-Wins conflict rule compares. `deleted` crosses too, because deletions must
 * propagate: a hard delete can't be synced once the row is gone (hence soft delete).
 */
@Serializable
data class SessionDto(
    val id: String,
    val startedAt: Long,
    val name: String = "Session",
    val type: String = "STRENGTH",
    val notes: String? = null,
    val updatedAt: Long,
    val deleted: Boolean = false,
    // Template support (D2). Templates are precious User data, so they sync like any session; a
    // real session carries `templateId` provenance. Defaults keep this additive — payloads written
    // before these fields existed still deserialize.
    val isTemplate: Boolean = false,
    val source: String = "MANUAL",
    val templateId: String? = null,
    // The session's logged exercises + sets travel WITH it (aggregate sync). The session is the
    // sync unit; editing any set bumps the parent `updatedAt`, so aggregate LWW is correct and we
    // avoid per-set outbox/conflict granularity. Exercises themselves aren't sent — they're seeded
    // reference data on every device, referenced here by `exerciseId`.
    val loggedItems: List<LoggedItemDto> = emptyList(),
)

@Serializable
data class LoggedItemDto(
    val exerciseId: String,
    val orderIndex: Int,
    val sets: List<SetDto> = emptyList(),
)

@Serializable
data class SetDto(
    val setNumber: Int,
    // Performance (actuals):
    val reps: Int? = null,
    val loadKg: Double? = null,
    val timeSec: Int? = null,
    val distanceM: Int? = null,
    val rpe: Int? = null,
    // Prescription (targets) — populated for template sets, copied on instantiation:
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null,
    val targetTimeSec: Int? = null,
    val targetDistanceM: Int? = null,
)
