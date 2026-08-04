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

enum class OnboardingStep { ATHLETE_PROFILE, RACE_CONFIG }
enum class Gender { WOMEN, MEN }
enum class Tier { OPEN, PRO }
enum class RaceFormat { SINGLES, DOUBLES, RELAY }

class OnboardingViewModel(
    private val repository: AthleteProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onFullName(value: String) = _state.update { it.copy(fullName = value) }
    fun onBodyweight(value: String) =
        _state.update { it.copy(bodyweightKg = value.sanitizeDecimal()) }

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

private fun divisionKey(gender: Gender?, tier: Tier?): String {
    val base = if (gender == Gender.WOMEN) "WOMEN" else "MEN"
    return if (tier == Tier.PRO) "${base}_PRO" else base
}

private fun String.sanitizeDecimal(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val dot = filtered.indexOf('.')
    return if (dot == -1) filtered else filtered.substring(0, dot + 1) + filtered.substring(dot + 1)
        .replace(".", "")
}

private fun String.step(delta: Int, min: Int, max: Int): String {
    val current = toDoubleOrNull()?.toInt() ?: 0
    return (current + delta).coerceIn(min, max).toString()
}
