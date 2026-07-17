package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.MuscleDiagram
import dev.cadence.data.MuscleImageProvider
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads one catalog [Exercise] for the Exercise Detail screen, plus (best-effort) a wger muscle
 * diagram URL for its primary muscle — the network only feeds this StateFlow; the UI observes it.
 */
class ExerciseDetailViewModel(
    repository: SessionRepository,
    muscleImages: MuscleImageProvider,
    exerciseId: String,
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    private val _muscleDiagram = MutableStateFlow<MuscleDiagram?>(null)
    val muscleDiagram: StateFlow<MuscleDiagram?> = _muscleDiagram.asStateFlow()

    init {
        viewModelScope.launch {
            val ex = repository.exerciseById(exerciseId)
            _exercise.value = ex
            ex?.primaryMusclesList?.firstOrNull()?.let { muscle ->
                _muscleDiagram.value = muscleImages.diagram(muscle)
            }
        }
    }
}
