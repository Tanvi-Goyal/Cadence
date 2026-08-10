@file:OptIn(ExperimentalTime::class)

package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.LoggedItemUi
import com.mindset.domain.repository.SessionRepository
import com.mindset.domain.toLoggedItemUis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.ExperimentalTime

data class SessionDetailUiState(
    val name: String = "",
    val type: String = "",
    val startedAt: Long = 0L,
    val totalVolumeKg: Double = 0.0,
    val items: List<LoggedItemUi> = emptyList(),
)

/**
 * Read-only view of a past session ([sessionId]) — reuses the same hydrated read as Log Workout but
 * exposes no mutations. Parameterized by sessionId (resolved via Koin `parametersOf`).
 */
class SessionDetailViewModel(private val repository: SessionRepository, private val sessionId: String) : ViewModel() {
    val uiState: StateFlow<SessionDetailUiState> =
        repository
            .observeSessionDetail(sessionId)
            .map { detail ->
                val itemUis = detail.toLoggedItemUis()
                val volume =
                    itemUis.sumOf { item ->
                        item.sets.sumOf { (it.reps ?: 0) * (it.loadKg ?: 0.0) }
                    }
                SessionDetailUiState(
                    name = detail?.session?.name.orEmpty(),
                    type =
                    detail
                        ?.session
                        ?.type
                        ?.name
                        .orEmpty(),
                    startedAt = detail?.session?.startedAt?.toEpochMilliseconds() ?: 0L,
                    totalVolumeKg = volume,
                    items = itemUis,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SessionDetailUiState(),
            )
}
