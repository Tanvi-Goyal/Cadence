package dev.cadence.domain

import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the user's device-local [UserPreferences]. Implementations back this with local
 * storage; the UI observes it as a [Flow] and never sees storage details.
 */
interface PreferencesRepository {
    /** Emits the current preferences, falling back to defaults when nothing has been written yet. */
    fun observe(): Flow<UserPreferences>

    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setThemeMode(mode: ThemeMode)
}
