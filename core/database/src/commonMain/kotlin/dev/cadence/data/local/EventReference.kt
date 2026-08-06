package dev.cadence.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Event-format reference data (iteration 3) — the verified race format as DATA, generalized past Hyrox
 * so the next event is pure seed rows, not a migration. Four tables (they replace the v10 `hyrox_*`
 * tables):
 *  - [EventFormatEntity]     supported event types (v1: HYROX; DEKA / CrossFit later = seed rows).
 *  - [EventSegmentEntity]    the ordered run / station / transition segments of a format (Hyrox = 16).
 *  - [EventDivisionEntity]   the gender × tier divisions of a format.
 *  - [SegmentStandardEntity] the per-division, per-mode loads / reps / targets for a segment.
 *
 * Seeded REFERENCE data — identical on every device, so (like [Exercise]) it carries NO sync envelope
 * and NO outbox row. Stable slug ids. Suffixed `Entity` to avoid colliding with the
 * `dev.cadence.model` domain types (EventFormat / EventSegment / EventDivision / SegmentStandard).
 */
@Entity(tableName = "event_format")
data class EventFormatEntity(
    @PrimaryKey val formatKey: String,
    val name: String,
    val description: String = "",
)

@Entity(tableName = "event_segment", indices = [Index("formatKey")])
data class EventSegmentEntity(
    @PrimaryKey val id: String,          // slug, e.g. "hyrox:04-sled-push", "hyrox:03-run"
    val formatKey: String,
    val orderIndex: Int,                 // race order (Hyrox 1..16: run, station, run, station, …)
    val kind: String,                    // dev.cadence.model.SegmentKind name (RUN/STATION/TRANSITION)
    val exerciseId: String,              // → catalog Exercise id (hyrox-run, hyrox-ski-erg, …)
    val name: String,
    val label: String = "",              // thematic grouping, e.g. "Block 1: Start"
    val metric: String,                  // dev.cadence.model.MetricType name
    val distanceM: Int? = null,          // fixed distance (null for rep-scored)
    val reps: Int? = null,               // fixed reps (wall balls; null otherwise)
    val loadType: String? = null,        // which per-division load applies (null = bodyweight/erg/run)
    val descriptor: String = "",
)

@Entity(tableName = "event_division", indices = [Index("formatKey")])
data class EventDivisionEntity(
    @PrimaryKey val id: String,          // "HYROX:MEN"
    val formatKey: String,
    val key: String,                     // WOMEN / MEN / WOMEN_PRO / MEN_PRO (stored on sessions/goals/PRs)
    val label: String,
    val gender: String,                  // dev.cadence.model.Gender name
    val tier: String,                    // dev.cadence.model.Tier name
    val orderIndex: Int,
)

@Entity(
    tableName = "segment_standard",
    indices = [Index("formatKey"), Index("divisionKey"), Index("segmentId")],
)
data class SegmentStandardEntity(
    @PrimaryKey val id: String,          // "HYROX:MEN:04-sled-push"
    val formatKey: String,
    val divisionKey: String,
    val segmentId: String,
    val mode: String,                    // dev.cadence.model.RaceMode name (v1: SINGLES)
    val loadKg: Double? = null,          // canonical weight (e.g. 152.0)
    val loadDisplay: String? = null,     // rulebook string, e.g. "2×24 kg"
    val targetReps: Int? = null,         // division-specific reps (e.g. wall balls 100)
    val targetDistanceM: Int? = null,    // division-specific distance override
    val targetHeightM: Double? = null,   // wall-ball target height (2.7 / 3.0)
)
