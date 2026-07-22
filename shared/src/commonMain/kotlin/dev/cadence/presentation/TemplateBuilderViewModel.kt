package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TemplateBuilderUiState(
    val templateName: String = "",
    val items: List<LoggedItemUi> = emptyList(),
)

/**
 * Drives the template builder for one template [templateId]. Structurally a sibling of
 * [LogWorkoutViewModel] — same hydrated read — but its writes populate the **prescription**
 * (`target*`) via [SessionRepository.addTargetSet], because a template captures the plan.
 */
class TemplateBuilderViewModel(
    private val repository: SessionRepository,
    private val templateId: String,
) : ViewModel() {

    val uiState: StateFlow<TemplateBuilderUiState> =
        repository.observeSessionDetail(templateId).map { detail ->
            TemplateBuilderUiState(
                templateName = detail?.session?.name.orEmpty(),
                items = detail.toLoggedItemUis(),
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
