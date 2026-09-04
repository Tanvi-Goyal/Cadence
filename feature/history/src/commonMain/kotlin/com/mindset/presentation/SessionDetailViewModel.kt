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
    /**
     * Training time: the sum of the logged segment splits (runs **and** stations). Null when nothing
     * was timed — a strength session shows volume instead, so there is no duration to invent.
     */
    val totalTimeMs: Long? = null,
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
                val session = detail?.session
                val startedAt = session?.startedAt?.toEpochMilliseconds() ?: 0L
                // Sum the splits rather than the wall clock. `finishedAt − startedAt` measured the gap
                // between the FAB creating the row and Complete being tapped — for a session logged
                // after the fact that is just typing time, and for the live timer it re-counts the
                // pauses the split accumulator deliberately leaves out. Every timed set counts, so
                // runs are included by construction.
                val loggedSec = itemUis.sumOf { item -> item.sets.sumOf { it.timeSec ?: 0 } }
                SessionDetailUiState(
                    name = session?.name.orEmpty(),
                    type = session?.type?.name.orEmpty(),
                    startedAt = startedAt,
                    totalTimeMs = loggedSec.takeIf { it > 0 }?.times(1000L),
                    totalVolumeKg = volume,
                    items = itemUis,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SessionDetailUiState(),
            )
}
