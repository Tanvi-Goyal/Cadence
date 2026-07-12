package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Exercise
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

/** Backs the searchable exercise picker. The paged feed re-queries as the search text changes. */
class ExerciseLibraryViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    val queryText: StateFlow<String> = query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: Flow<PagingData<Exercise>> =
        query
            .flatMapLatest { repository.searchExercises(it) }
            .cachedIn(viewModelScope)

    fun onQueryChange(text: String) {
        query.value = text
    }
}
