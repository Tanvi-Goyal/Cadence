package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.SessionRepository
import dev.cadence.model.SessionDetail
import dev.cadence.model.SetEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One exercise card in Log Workout, resolved for display (name + metric + its sets). */
data class LoggedItemUi(
    val loggedItemId: String,
    val exerciseName: String,
    val metric: String,
    val sets: List<SetEntry>,
)

/**
 * Flatten a hydrated [SessionDetail] into the per-exercise cards the logging/detail/builder screens
 * render. `metric` is the domain [MetricType] name, so the existing `== ExerciseMetric.WEIGHT_REPS`
 * check in the UI still selects the strength layout. (Blocks are flattened for now; the block-aware
 * UI arrives with the real block model.)
 */
internal fun SessionDetail?.toLoggedItemUis(): List<LoggedItemUi> =
    this?.blocks?.flatMap { it.entries }?.map { entry ->
        LoggedItemUi(
            loggedItemId = entry.entry.id,
            exerciseName = entry.exercise.name,
            metric = entry.exercise.defaultMetric.name,
            sets = entry.sets,
        )
    }.orEmpty()

data class LogWorkoutUiState(
    val sessionName: String = "",
    val items: List<LoggedItemUi> = emptyList(),
)

/**
 * Drives the Log Workout screen for one [sessionId]. Reads the hydrated session from the DB (never
 * the network); writes go through the repository, which bumps the parent session so the whole
 * aggregate re-syncs.
 */
class LogWorkoutViewModel(
    private val repository: SessionRepository,
    private val sessionId: String,
) : ViewModel() {

    val uiState: StateFlow<LogWorkoutUiState> =
        repository.observeSessionDetail(sessionId).map { detail ->
            LogWorkoutUiState(
                sessionName = detail?.session?.name.orEmpty(),
                items = detail.toLoggedItemUis(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LogWorkoutUiState(),
        )

    fun addExercise(exerciseId: String) {
        viewModelScope.launch { repository.addExercise(sessionId, exerciseId) }
    }

    fun addStrengthSet(loggedItemId: String, reps: Int, loadKg: Double) {
        viewModelScope.launch {
            repository.addSet(sessionId, loggedItemId, reps = reps, loadKg = loadKg)
        }
    }

    fun addCardioSet(loggedItemId: String, timeSec: Int, distanceM: Int) {
        viewModelScope.launch {
            repository.addSet(sessionId, loggedItemId, timeSec = timeSec, distanceM = distanceM)
        }
    }

    /** Fill in a set's actual performance against its (possibly ghost) target — strength. */
    fun updateStrengthActual(set: SetEntry, reps: Int, loadKg: Double) {
        viewModelScope.launch {
            repository.updateSet(sessionId, set.copy(reps = reps, loadKg = loadKg))
        }
    }

    /** Fill in a set's actual performance against its (possibly ghost) target — conditioning. */
    fun updateCardioActual(set: SetEntry, timeSec: Int, distanceM: Int) {
        viewModelScope.launch {
            repository.updateSet(sessionId, set.copy(timeSec = timeSec, distanceM = distanceM))
        }
    }
}
