package dev.cadence.model

/**
 * A supported competition/event format. Iteration-3 generalization of the previously Hyrox-specific
 * reference data: Hyrox is the v1 seeded instance, and DEKA / CrossFit arrive later as pure seed rows
 * (new [EventFormat] + [EventSegment]/[EventDivision]/[SegmentStandard] rows) with **no migration**.
 *
 * Reference data — identical seeded rows on every device, referenced by stable-slug key, never synced.
 */
data class EventFormat(
    val formatKey: String,
    val name: String,
    val description: String = "",
) {
    companion object {
        /** The v1 event. Used as the [formatKey] on [RaceGoal]s and race [Session]s. */
        const val HYROX = "HYROX"
    }
}

/**
 * One ordered segment of an [EventFormat] — a [SegmentKind.RUN] leg, a functional [SegmentKind.STATION],
 * or a [SegmentKind.TRANSITION] (roxzone). Hyrox resolves to 16 segments (8 runs interleaved with 8
 * stations). Each segment is backed by a catalog [Exercise] so a captured segment is real, queryable
 * data; [metric] drives which capture fields the log row shows.
 */
data class EventSegment(
    val id: String,
    val formatKey: String,
    val orderIndex: Int,
    val kind: SegmentKind,
    val exerciseId: String,
    val name: String,
    val label: String = "",
    val metric: MetricType,
    val distanceM: Int? = null,
    val reps: Int? = null,
    /** Which per-division load this segment uses (a [SegmentStandard.loadType]); null = bodyweight/erg/run. */
    val loadType: String? = null,
    val descriptor: String = "",
)

/**
 * A division of an [EventFormat] — the [gender] × [tier] weight/standard class. [key] (e.g. `MEN`,
 * `WOMEN_PRO`) is the stable value persisted on [RaceGoal]s, race [Session]s, and [PersonalRecord]s.
 */
data class EventDivision(
    val id: String,
    val formatKey: String,
    val key: String,
    val label: String,
    val gender: Gender,
    val tier: Tier,
    val orderIndex: Int,
)

/**
 * Per-division parameters (loads / reps / targets) for one [EventSegment] in one [RaceMode]. This is
 * where "Sled Push · Men · 152 kg" and "Wall Balls · Men · 100 reps @ 3 m" live. v1 seeds
 * [RaceMode.SINGLES] only; Doubles/Relay standards are additive rows later.
 */
data class SegmentStandard(
    val id: String,
    val formatKey: String,
    val divisionKey: String,
    val segmentId: String,
    val mode: RaceMode = RaceMode.SINGLES,
    val loadKg: Double? = null,
    val loadDisplay: String? = null,
    val targetReps: Int? = null,
    val targetDistanceM: Int? = null,
    val targetHeightM: Double? = null,
)
