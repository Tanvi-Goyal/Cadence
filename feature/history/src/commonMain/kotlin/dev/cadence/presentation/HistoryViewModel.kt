package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.SessionRepository
import dev.cadence.model.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** A history row: the session plus its computed volume. */
data class HistoryRow(
    val session: Session,
    val volumeKg: Double,
)

/**
 * Backs the History tab — the full session list, reactive from the DB (never the network). Reuses
 * the same session + volume flows Home uses; History is just the unfiltered view.
 */
class HistoryViewModel(
    repository: SessionRepository,
) : ViewModel() {

    val rows: StateFlow<List<HistoryRow>> =
        combine(
            repository.observeSessions(),
            repository.observeVolumesBySession(),
        ) { sessions, volumes ->
            sessions.map { HistoryRow(it, volumes[it.id] ?: 0.0) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
