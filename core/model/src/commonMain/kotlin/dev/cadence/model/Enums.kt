package dev.cadence.model

/**
 * Training modality — first-class on every [Exercise] and rolled up per [Block]/[Session] in
 * queries. This is what makes Cadence a *hybrid* tracker rather than a strength logger with cardio
 * bolted on: templates, load, and insights all key off modality.
 */
enum class Modality { STRENGTH, CONDITIONING, RUN, MOBILITY }

/**
 * Which capture fields a set shows *by default*. A [SetEntry] still holds every metric as a nullable
 * value; this only drives the Live Logging row's inputs. See [CaptureFields].
 */
enum class MetricType { WEIGHT_REPS, REPS_ONLY, DISTANCE_TIME, DURATION, CALORIES }

/** How the exercises inside a [Block] are structured (straight sets vs supersets/circuits/etc.). */
enum class BlockType { STRAIGHT, SUPERSET, CIRCUIT, INTERVAL, RUN }

/** Which phase of the workout a [Block] belongs to — drives grouping/ordering on the session detail. */
enum class BlockSection { WARMUP, MAIN, ACCESSORY, CONDITIONING, CORE }

/**
 * The shape of a conditioning [Block]. AMRAP = as many rounds as possible within `capSeconds`; EMOM =
 * one movement per minute for `capSeconds`; TABATA = `workSeconds` on / `restBetweenRoundsMs` off ×
 * `rounds`; FOR_TIME = complete the prescribed work as fast as possible (optional `capSeconds` cap).
 */
enum class ConditioningFormat { AMRAP, EMOM, TABATA, FOR_TIME }

/** The session's dominant modality mix, used for badges/filtering. */
enum class SessionType { STRENGTH, CONDITIONING, HYROX, MIXED }

/** How a session came to exist. RACE_SIM / HEALTH_CONNECT are reserved for later phases. */
enum class SessionSource { MANUAL, FROM_TEMPLATE, RACE_SIM, HEALTH_CONNECT }

/** The 8 Hyrox stations. A non-null value on [Exercise.hyroxStation] tags it as a Hyrox station. */
enum class HyroxStation {
    SKI_ERG, SLED_PUSH, SLED_PULL, BURPEE_BROAD_JUMP,
    ROWING, FARMERS_CARRY, SANDBAG_LUNGES, WALL_BALLS,
}

/** The kinds of personal record Cadence tracks (per exercise, and per distance bucket for time). */
enum class PrKind { EST_1RM, MAX_WEIGHT, MAX_REPS, BEST_TIME, MAX_CALORIES }

// --- Event-format model (iteration 3) --------------------------------------------------------------
// These generalize the previously Hyrox-specific reference data. Hyrox is the v1 seeded instance;
// DEKA / CrossFit arrive later as pure seed rows (no schema change).

/**
 * A segment of an [EventFormat] is a RUN leg, a functional STATION, or a TRANSITION (roxzone).
 * Modelling runs as explicit segments (rather than a "run before" label on a station) makes the
 * race timeline and per-run pace an ordered read instead of an inference.
 */
enum class SegmentKind { RUN, STATION, TRANSITION }

/**
 * How a race is contested. v1 seeds SINGLES standards and captures single-athlete work; DOUBLES /
 * RELAY loads are seedable later as additional [SegmentStandard] rows with no migration.
 */
enum class RaceMode { SINGLES, DOUBLES, RELAY }

/** Lifecycle of a [RaceGoal] — Home reads the next [UPCOMING]; past races become [COMPLETED]. */
enum class RaceGoalStatus { UPCOMING, COMPLETED, ABANDONED }

/** Division axis: competitor gender category. */
enum class Gender { WOMEN, MEN }

/** Division axis: open (standard) vs pro (elite) weights. */
enum class Tier { OPEN, PRO }
