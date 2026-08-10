package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.ThemeMode
import com.mindset.domain.UserPreferences
import com.mindset.domain.WeightUnit
import com.mindset.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the Profile / Preferences screen on both platforms. Exposes the reactive [UserPreferences]
 * and two intents; writes go through the repository (DB), and [preferences] re-emits from the DB —
 * same observe-the-DB flow as every other screen.
 */
class PreferencesViewModel(private val repository: PreferencesRepository) : ViewModel() {
    val preferences: StateFlow<UserPreferences> =
        repository.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences(),
        )

    fun onWeightUnitChange(unit: WeightUnit) {
        viewModelScope.launch { repository.setWeightUnit(unit) }
    }

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }
}
