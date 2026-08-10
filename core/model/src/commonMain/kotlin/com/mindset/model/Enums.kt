package com.mindset.model

/** The kinds of personal record MindSet tracks (per exercise, and per distance bucket for time). */
enum class PrKind { EST_1RM, MAX_WEIGHT, MAX_REPS, BEST_TIME, MAX_CALORIES }

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
