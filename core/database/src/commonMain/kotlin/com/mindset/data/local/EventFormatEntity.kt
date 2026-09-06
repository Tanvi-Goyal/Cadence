package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Event-format reference data (iteration 3) — the verified race format as DATA, generalized past Hyrox
 * so the next event is pure seed rows, not a migration. Four tables (they replace the v10 `hyrox_*`
 * tables):
 *  - [EventFormatEntity]     supported event types (v1: HYROX; DEKA / CrossFit later = seed rows).
 *  - [EventSegmentEntity]    the ordered run / station / transition segments of a format (Hyrox = 16).
 *  - [EventDivisionEntity]   the gender × tier divisions of a format.
 *  - [EventSegmentStandardEntity] the per-division, per-mode loads / reps / targets for a segment.
 *
 * Seeded REFERENCE data — identical on every device, so (like [Exercise]) it carries NO sync envelope
 * and NO outbox row. Stable slug ids. Suffixed `Entity` to avoid colliding with the
 * `com.mindset.model` domain types (EventFormat / EventSegment / EventDivision / SegmentStandard).
 */
@Entity(tableName = "event_format")
data class EventFormatEntity(
    @PrimaryKey val formatKey: String,
    val name: String,
    val description: String = "",
)
