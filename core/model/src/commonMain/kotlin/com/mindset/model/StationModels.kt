package com.mindset.model

/**
 * Raw projection of a cached Hyrox PB joined to its station exercise — the DAO output for the Station
 * board. The repository maps it to a [StationRecord] (resolving the enum columns). [achievedAt] is
 * epoch-millis; [distanceBucketM] is metres for a `DISTANCE_TIME` PB, the rep target for a
 * `REPS_TIME` PB (e.g. 100 wall balls), null otherwise.
 */
/**
 * How many sessions logged an actual time for a station, within one division. Raw projection; the
 * repository keys it by [HyroxStation].
 */
/**
 * The two history metrics the Station board shows beside a PB, for one division: the most recently
 * logged time ([recentTimeSec], null until the station has been logged at this race weight) and how
 * many sessions logged it ([sessionCount]).
 */
data class StationAggregate(
    val recentTimeSec: Int?,
    /**
     * The time logged in the session *before* [recentTimeSec], at the same race weight — the baseline
     * the board's trend chip measures against. Null until the station has two logged sessions.
     */
    val previousTimeSec: Int?,
    val sessionCount: Int,
)

data class StationSessionCountRow(val hyroxStation: String, val sessionCount: Int)

/**
 * One logged split for a station, within one division. [sessionId] is what identifies "the previous
 * time" — grouping by [startedAt] would merge two sessions created in the same millisecond.
 */
data class StationRecentRow(val hyroxStation: String, val sessionId: String, val timeSec: Int, val startedAt: Long)

data class HyroxRecordRow(
    val exerciseId: String,
    val exerciseName: String,
    val hyroxStation: String,
    val kind: String,
    val value: Double,
    val distanceBucketM: Int?,
    val divisionKey: String?,
    val achievedAt: Long,
)

/**
 * A per-station personal record, bucketed by weight class ([divisionKey]) and effort ([bucket]).
 * [value] is interpreted by [kind]: seconds for [PrKind.BEST_TIME], reps for [PrKind.MAX_REPS], kg
 * for [PrKind.MAX_WEIGHT]/[PrKind.EST_1RM], calories for [PrKind.MAX_CALORIES]. [bucket] is the
 * distance in metres or the rep target the time was set over (null for kind-only records).
 */
data class StationRecord(
    val station: HyroxStation,
    val displayName: String,
    val kind: PrKind,
    val value: Double,
    val bucket: Int?,
    val divisionKey: String?,
    val achievedAtMillis: Long,
)
