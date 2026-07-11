package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Immutable UI state for the Home screen. */
data class HomeUiState(
    val sessions: List<Session> = emptyList(),
)

/**
 * Shared (KMP) ViewModel for Home. Exposes a single [StateFlow] via `stateIn` rather than a raw
 * flow, so the UI gets a hot, lifecycle-friendly stream with a cached latest value. It observes
 * the DB (never the network) — the offline-first read path.
 */
class HomeViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        repository.observeSessions()
            .map { HomeUiState(sessions = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )

    /** Intent: create a new blank session. The write is local-first and instant. */
    fun onNewSessionClick() {
        viewModelScope.launch {
            repository.createSession()
        }
    }
}
