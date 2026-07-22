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
