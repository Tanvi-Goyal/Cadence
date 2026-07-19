package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Exercise
import dev.cadence.data.local.ExerciseMetric
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TemplateBuilderUiState(
    val templateName: String = "",
    val items: List<LoggedItemUi> = emptyList(),
)

/**
 * Drives the template builder for one template [templateId]. Structurally a sibling of
 * [LogWorkoutViewModel] — same DB reads (session + logged items/sets + exercise catalog) — but its
 * writes populate the **prescription** (`target*`) via [SessionRepository.addTargetSet], because a
 * template captures the plan, not a performance.
 */
class TemplateBuilderViewModel(
    private val repository: SessionRepository,
    private val templateId: String,
) : ViewModel() {

    private val exercisesById = MutableStateFlow<Map<String, Exercise>>(emptyMap())

    init {
        viewModelScope.launch { exercisesById.value = repository.exercisesById() }
    }

    val uiState: StateFlow<TemplateBuilderUiState> =
        combine(
            repository.observeSession(templateId),
            repository.observeLoggedItems(templateId),
            exercisesById,
        ) { template, items, catalog ->
            TemplateBuilderUiState(
                templateName = template?.name.orEmpty(),
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
            initialValue = TemplateBuilderUiState(),
        )

    fun addExercise(exerciseId: String) {
        viewModelScope.launch { repository.addExercise(templateId, exerciseId) }
    }

    fun addTargetStrengthSet(loggedItemId: String, reps: Int, loadKg: Double) {
        viewModelScope.launch {
            repository.addTargetSet(templateId, loggedItemId, reps = reps, loadKg = loadKg)
        }
    }

    fun addTargetCardioSet(loggedItemId: String, timeSec: Int, distanceM: Int) {
        viewModelScope.launch {
            repository.addTargetSet(templateId, loggedItemId, timeSec = timeSec, distanceM = distanceM)
        }
    }
}
