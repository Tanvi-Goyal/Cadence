package dev.cadence.domain

import kotlinx.coroutines.flow.Flow

/**
 * The athlete's profile + target race, captured during Onboarding. Device-local (never synced).
 * [defaultDivision] is a `hyrox_divisions.key` (WOMEN/MEN/WOMEN_PRO/MEN_PRO) used as the default for
 * Hyrox workouts; [raceFormat] is the competition format (e.g. SINGLES/DOUBLES/RELAY). [onboardingComplete]
 * gates whether the app opens on Home or Onboarding.
 */
data class AthleteProfile(
    val fullName: String = "",
    val bodyweightKg: Double? = null,
    val heightCm: Double? = null,
    val defaultDivision: String = "MEN",
    val raceDate: Long? = null,
    val raceFormat: String? = null,
    val raceCity: String? = null,
    val onboardingComplete: Boolean = false,
)

/**
 * Reads and writes the device-local [AthleteProfile]. The UI observes it as a [Flow] and never sees
 * storage details; [observe] falls back to defaults when Onboarding hasn't run yet.
 */
interface AthleteProfileRepository {
    fun observe(): Flow<AthleteProfile>

    /** Persists the whole profile (Onboarding "complete"). */
    suspend fun save(profile: AthleteProfile)

    /** Flips just the onboarding flag, preserving the rest of the profile. */
    suspend fun setOnboardingComplete(complete: Boolean)
}
