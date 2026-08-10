package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.ExerciseRef
import com.mindset.model.VolumePoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val exercises: List<ExerciseRef> = emptyList(),
    val selectedExerciseId: String? = null,
    val points: List<VolumePoint> = emptyList(),
)

/**
 * Backs the Stats tab: a selector of exercises-with-history + the selected exercise's volume trend.
 * The chart flow re-queries whenever the selection changes ([flatMapLatest]).
 */
class StatsViewModel(private val repository: SessionRepository) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    private val exercises = repository.observeExercisesWithHistory()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> =
        combine(exercises, selected) { list, sel -> list to (sel ?: list.firstOrNull()?.id) }
            .flatMapLatest { (list, id) ->
                if (id == null) {
                    flowOf(StatsUiState(exercises = list))
                } else {
                    repository.observeVolumeOverTime(id).map { points ->
                        StatsUiState(exercises = list, selectedExerciseId = id, points = points)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = StatsUiState(),
            )

    fun onSelectExercise(exerciseId: String) {
        selected.value = exerciseId
    }
}
