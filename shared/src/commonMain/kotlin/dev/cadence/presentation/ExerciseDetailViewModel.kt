package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads one catalog [Exercise] for the Exercise Detail screen. */
class ExerciseDetailViewModel(
    repository: SessionRepository,
    exerciseId: String,
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    init {
        viewModelScope.launch { _exercise.value = repository.exerciseById(exerciseId) }
    }
}
