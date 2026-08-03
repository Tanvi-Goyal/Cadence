package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.LogSectionUi
import dev.cadence.domain.SessionRepository
import dev.cadence.domain.toLogSections
import dev.cadence.model.SetEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LogWorkoutUiState(
    val sessionName: String = "",
    val sections: List<LogSectionUi> = emptyList(),
)

/**
 * Drives the Log Workout screen for one [sessionId]. Reads the hydrated session from the DB (never
 * the network) as ordered blocks/sections; writes go through the repository, which bumps the parent
 * session so the whole aggregate re-syncs (and runs PB detection on real actuals).
 */
class LogWorkoutViewModel(
    private val repository: SessionRepository,
    private val sessionId: String,
) : ViewModel() {

    val uiState: StateFlow<LogWorkoutUiState> =
        repository.observeSessionDetail(sessionId).map { detail ->
            LogWorkoutUiState(
                sessionName = detail?.session?.name.orEmpty(),
                sections = detail.toLogSections(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LogWorkoutUiState(),
        )

    fun addExercise(exerciseId: String) {
        viewModelScope.launch { repository.addExercise(sessionId, exerciseId) }
    }

    /** Append a new actual set (beyond the prescription) — metric-agnostic; pass only the relevant cells. */
    fun addSet(loggedItemId: String, reps: Int? = null, loadKg: Double? = null, timeSec: Int? = null, distanceM: Int? = null) {
        viewModelScope.launch {
            repository.addSet(sessionId, loggedItemId, reps = reps, loadKg = loadKg, timeSec = timeSec, distanceM = distanceM)
        }
    }

    /** Persist a set's actuals against its (possibly ghost) target. The screen builds the metric-specific
     *  `copy` (reps/load, reps, time, …) so one path serves every capture type. */
    fun updateActual(set: SetEntry) {
        viewModelScope.launch { repository.updateSet(sessionId, set) }
    }
}
