package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Exercise
import dev.cadence.data.local.ExerciseMetric
import dev.cadence.data.local.SetEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One exercise card in Log Workout, resolved for display (name + metric + its sets). */
data class LoggedItemUi(
    val loggedItemId: String,
    val exerciseName: String,
    val metric: String,
    val sets: List<SetEntry>,
)

data class LogWorkoutUiState(
    val sessionName: String = "",
    val items: List<LoggedItemUi> = emptyList(),
)

/**
 * Drives the Log Workout screen for one [sessionId]. Reads the session + its logged items/sets
 * from the DB (never the network); writes go through the repository, which bumps the parent
 * session so the whole aggregate re-syncs.
 */
class LogWorkoutViewModel(
    private val repository: SessionRepository,
    private val sessionId: String,
) : ViewModel() {

    private val exercisesById = MutableStateFlow<Map<String, Exercise>>(emptyMap())

    init {
        viewModelScope.launch { exercisesById.value = repository.exercisesById() }
    }

    val uiState: StateFlow<LogWorkoutUiState> =
        combine(
            repository.observeSession(sessionId),
            repository.observeLoggedItems(sessionId),
            exercisesById,
        ) { session, items, catalog ->
            LogWorkoutUiState(
                sessionName = session?.name.orEmpty(),
                items = items.map { withSets ->
                    val exercise = catalog[withSets.item.exerciseId]
                    LoggedItemUi(
                        loggedItemId = withSets.item.id,
                        exerciseName = exercise?.name ?: withSets.item.exerciseId,
                        metric = exercise?.metric ?: ExerciseMetric.WEIGHT_REPS,
                        sets = withSets.sets,
                    )
                },
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
