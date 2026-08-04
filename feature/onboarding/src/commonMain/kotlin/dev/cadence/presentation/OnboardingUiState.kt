package dev.cadence.presentation

data class OnboardingUiState(
    val stepIndex: Int = 0,
    val fullName: String = "",
    val bodyweightKg: String = "",
    val heightIn: String = "",
    val raceDateMillis: Long? = null,
    val gender: Gender? = null,
    val tier: Tier? = null,
    val format: RaceFormat? = null,
    val raceCity: String = "",
    val saving: Boolean = false,
    /** Set once the profile is persisted; the UI observes this to leave onboarding. */
    val done: Boolean = false,
) {
    val step: OnboardingStep get() = OnboardingStep.entries[stepIndex]
    val stepCount: Int get() = OnboardingStep.entries.size
    val isFirst: Boolean get() = stepIndex == 0
    val isLast: Boolean get() = stepIndex == OnboardingStep.entries.lastIndex

    /** Whether the current step's required fields are filled (gates Next / Complete). */
    val currentStepValid: Boolean
        get() = when (step) {
            OnboardingStep.ATHLETE_PROFILE -> fullName.isNotBlank()
            OnboardingStep.RACE_CONFIG ->
                raceDateMillis != null && gender != null && tier != null && format != null
        }
}