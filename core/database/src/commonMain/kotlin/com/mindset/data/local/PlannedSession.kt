package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A planned/next session shown on the Home "Today" card. Deliberately LOCAL-ONLY — plans are not
 * part of the sync contract (no DTO, no outbox). Program planning across devices is a v2 concern;
 * for now one seeded default plan gives the card real content without a program builder.
 */
@Entity(tableName = "planned_sessions")
data class PlannedSession(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val targetDurationMin: Int,
    val focus: String,
)
