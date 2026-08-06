package dev.cadence.domain

import kotlinx.coroutines.flow.Flow

/**
 * The athlete's identity + physical baseline, captured during Onboarding. Device-local (never synced;
 * Phase-2 auth keys it to a user). The **target race no longer lives here** — it is a first-class,
 * multi-instance [dev.cadence.model.RaceGoal] (see [RaceGoalRepository]); this row keeps only identity
 * and the defaults that pre-fill new goals and sims. [defaultDivisionKey] is an `event_division.key`
 * (WOMEN/MEN/WOMEN_PRO/MEN_PRO); [defaultMode] is the preferred [dev.cadence.model.RaceMode] name.
 * [onboardingComplete] gates whether the app opens on Home or Onboarding.
 */
data class AthleteProfile(
    val fullName: String = "",
    val bodyweightKg: Double? = null,
    val heightCm: Double? = null,
)
