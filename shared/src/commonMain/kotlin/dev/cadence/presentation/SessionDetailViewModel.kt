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

data class SessionDetailUiState(
    val name: String = "",
    val type: String = "",
    val startedAt: Long = 0L,
    val totalVolumeKg: Double = 0.0,
    val items: List<LoggedItemUi> = emptyList(),
)

/**
 * Read-only view of a past session ([sessionId]) — reuses the same DB flows as Log Workout but
 * exposes no mutations. Parameterized by sessionId (resolved via Koin `parametersOf`).
 */
class SessionDetailViewModel(
    private val repository: SessionRepository,
    private val sessionId: String,
) : ViewModel() {

    private val exercisesById = MutableStateFlow<Map<String, Exercise>>(emptyMap())

    init {
        viewModelScope.launch { exercisesById.value = repository.exercisesById() }
    }

    val uiState: StateFlow<SessionDetailUiState> =
        combine(
            repository.observeSession(sessionId),
            repository.observeLoggedItems(sessionId),
            exercisesById,
        ) { session, items, catalog ->
            val itemUis = items.map { withSets ->
                val exercise = catalog[withSets.item.exerciseId]
                LoggedItemUi(
                    loggedItemId = withSets.item.id,
                    exerciseName = exercise?.name ?: withSets.item.exerciseId,
                    metric = exercise?.metric ?: ExerciseMetric.WEIGHT_REPS,
                    sets = withSets.sets,
                )
            }
            val volume = itemUis.sumOf { item ->
                item.sets.sumOf { (it.reps ?: 0) * (it.loadKg ?: 0.0) }
            }
            SessionDetailUiState(
                name = session?.name.orEmpty(),
                type = session?.type.orEmpty(),
                startedAt = session?.startedAt ?: 0L,
                totalVolumeKg = volume,
                items = itemUis,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionDetailUiState(),
        )
}
