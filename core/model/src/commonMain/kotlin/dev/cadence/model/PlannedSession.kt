package dev.cadence.model

/**
 * A planned/next session shown on the Home "Today" card. Local-only (not part of the sync contract);
 * the domain view of the Room `planned_sessions` row.
 */
data class PlannedSession(
    val id: String,
    val name: String,
    val type: SessionType,
    val targetDurationMin: Int,
    val focus: String,
)
