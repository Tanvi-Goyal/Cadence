package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.AthleteProfile
import dev.cadence.domain.AthleteProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The onboarding flow, modelled as an ordered list of [OnboardingStep]s so steps can be
 * added/removed by editing the enum (+ its content composable + validity branch) alone — nothing
 * else hardcodes the count. The progress indicator and Next/Complete logic derive from [OnboardingStep.entries].
 */
enum class OnboardingStep { ATHLETE_PROFILE, RACE_CONFIG }

/** Hyrox gender category. Combines with [Tier] to pick the weight division. */
enum class Gender { WOMEN, MEN }

/** Hyrox skill tier. WOMEN/MEN + OPEN/PRO map onto the four `hyrox_divisions` keys. */
enum class Tier { OPEN, PRO }

/** Hyrox competition format (stored as `AthleteProfile.raceFormat`). */
enum class RaceFormat { SINGLES, DOUBLES, RELAY }

/**
 * Immutable onboarding state. Numeric fields are held as text so the user can type freely (and the
 * +/- steppers edit the same text); they parse to numbers only at save time.
 */
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

/**
 * Drives [OnboardingUiState] and, on completion, persists the [AthleteProfile] (with
 * `onboardingComplete = true`) through [AthleteProfileRepository]. Height is captured in inches and
 * stored canonical cm; weight stays kg. Gender × Tier resolves to the `hyrox_divisions` key.
 */
class OnboardingViewModel(
    private val repository: AthleteProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onFullName(value: String) = _state.update { it.copy(fullName = value) }
    fun onBodyweight(value: String) = _state.update { it.copy(bodyweightKg = value.sanitizeDecimal()) }
    fun onHeight(value: String) = _state.update { it.copy(heightIn = value.sanitizeDecimal()) }
    fun onRaceDate(millis: Long?) = _state.update { it.copy(raceDateMillis = millis) }
    fun onGender(gender: Gender) = _state.update { it.copy(gender = gender) }
    fun onTier(tier: Tier) = _state.update { it.copy(tier = tier) }
    fun onFormat(format: RaceFormat) = _state.update { it.copy(format = format) }
    fun onCity(value: String) = _state.update { it.copy(raceCity = value) }

    /** Nudge bodyweight (kg) by [delta], clamped; edits the same text the field shows. */
    fun stepBodyweight(delta: Int) =
        _state.update { it.copy(bodyweightKg = it.bodyweightKg.step(delta, min = 0, max = 500)) }

    /** Nudge height (inches) by [delta], clamped. */
    fun stepHeight(delta: Int) =
        _state.update { it.copy(heightIn = it.heightIn.step(delta, min = 0, max = 108)) }

    fun onNext() = _state.update { if (it.isLast) it else it.copy(stepIndex = it.stepIndex + 1) }
    fun onBack() = _state.update { if (it.isFirst) it else it.copy(stepIndex = it.stepIndex - 1) }

    fun onComplete() {
        val s = _state.value
        if (!s.currentStepValid || s.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            repository.save(
                AthleteProfile(
                    fullName = s.fullName.trim(),
                    bodyweightKg = s.bodyweightKg.toDoubleOrNull(),
                    heightCm = s.heightIn.toDoubleOrNull()?.let { inches -> inches * INCH_TO_CM },
                    defaultDivision = divisionKey(s.gender, s.tier),
                    raceDate = s.raceDateMillis,
                    raceFormat = s.format?.name,
                    raceCity = s.raceCity.trim().ifBlank { null },
                    onboardingComplete = true,
                ),
            )
            _state.update { it.copy(saving = false, done = true) }
        }
    }
}

private const val INCH_TO_CM = 2.54

/** WOMEN/MEN × OPEN/PRO → the `hyrox_divisions.key`. Defaults to MEN/Open if unset. */
private fun divisionKey(gender: Gender?, tier: Tier?): String {
    val base = if (gender == Gender.WOMEN) "WOMEN" else "MEN"
    return if (tier == Tier.PRO) "${base}_PRO" else base
}

/** Keep digits and a single decimal point (defends the text-backed numeric fields). */
private fun String.sanitizeDecimal(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val dot = filtered.indexOf('.')
    return if (dot == -1) filtered else filtered.substring(0, dot + 1) + filtered.substring(dot + 1).replace(".", "")
}

/** Integer-step the (possibly empty/decimal) text by [delta], clamped to [min]..[max]. */
private fun String.step(delta: Int, min: Int, max: Int): String {
    val current = toDoubleOrNull()?.toInt() ?: 0
    return (current + delta).coerceIn(min, max).toString()
}
