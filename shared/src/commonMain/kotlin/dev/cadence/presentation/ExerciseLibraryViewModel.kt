package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.cadence.domain.SessionRepository
import dev.cadence.model.Exercise
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Backs the searchable exercise picker. The paged feed re-queries whenever the search text or the
 * selected muscle / equipment filter changes. Filters are single-select toggles (tap again = clear).
 */
class ExerciseLibraryViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val muscle = MutableStateFlow<String?>(null)
    private val equipment = MutableStateFlow<String?>(null)

    val queryText: StateFlow<String> = query.asStateFlow()
    val selectedMuscle: StateFlow<String?> = muscle.asStateFlow()
    val selectedEquipment: StateFlow<String?> = equipment.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: Flow<PagingData<Exercise>> =
        combine(query, equipment, muscle) { q, e, m -> Triple(q, e, m) }
            .flatMapLatest { (q, e, m) -> repository.searchExercises(q, e, m) }
            .cachedIn(viewModelScope)

    fun onQueryChange(text: String) {
        query.value = text
    }

    /** Toggle a muscle filter; selecting the active one clears it. */
    fun onMuscleToggle(value: String) {
        muscle.value = if (muscle.value == value) null else value
    }

    /** Toggle an equipment filter; selecting the active one clears it. */
    fun onEquipmentToggle(value: String) {
        equipment.value = if (equipment.value == value) null else value
    }
}
