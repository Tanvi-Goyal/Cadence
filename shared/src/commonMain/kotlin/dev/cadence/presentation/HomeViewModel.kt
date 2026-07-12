package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.Session
import dev.cadence.sync.SyncEngine
import dev.cadence.sync.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Where a sync pass currently stands, for the UI's sync indicator. */
enum class SyncStatusUi { Idle, Syncing, Error }

/** Immutable UI state for the Home screen. */
data class HomeUiState(
    val sessions: List<Session> = emptyList(),
    val syncStatus: SyncStatusUi = SyncStatusUi.Idle,
)

/**
 * Shared (KMP) ViewModel for Home. The session list comes from the DB Flow; the sync status is a
 * separate local UI flow, combined into one [StateFlow] via `stateIn`. The UI observes the DB,
 * never the network — the sync engine only feeds the DB.
 */
class HomeViewModel(
    private val repository: SessionRepository,
    private val syncEngine: SyncEngine,
) : ViewModel() {

    private val syncStatus = MutableStateFlow(SyncStatusUi.Idle)

    val uiState: StateFlow<HomeUiState> =
        combine(repository.observeSessions(), syncStatus) { sessions, status ->
            HomeUiState(sessions = sessions, syncStatus = status)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    init {
        // Sync-on-open: pull anything logged on another device since last time.
        sync()
    }

    /** Intent: create a new blank session. The write is local-first and instant. */
    fun onNewSessionClick() {
        viewModelScope.launch {
            repository.createSession()
        }
    }

    /** Intent: run a foreground sync pass (push the outbox, pull remote changes). */
    fun onSyncClick() {
        sync()
    }

    private fun sync() {
        viewModelScope.launch {
            syncStatus.value = SyncStatusUi.Syncing
            syncStatus.value = when (syncEngine.sync()) {
                is SyncResult.Success -> SyncStatusUi.Idle
                is SyncResult.Failure -> SyncStatusUi.Error
            }
        }
    }
}
