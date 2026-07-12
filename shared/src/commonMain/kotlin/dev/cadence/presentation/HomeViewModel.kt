package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import dev.cadence.sync.SyncEngine
import dev.cadence.sync.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Where a sync pass currently stands, for the UI's sync indicator. */
enum class SyncStatusUi { Idle, Syncing, Error }

/** Home summary stats, all derived from the session list (no volume — needs the set model). */
data class HomeStats(
    val total: Int = 0,
    val last7Days: Int = 0,
    val dayStreak: Int = 0,
)

/** Immutable UI state for the Home screen. */
data class HomeUiState(
    val sessions: List<Session> = emptyList(),
    val plannedSession: PlannedSession? = null,
    val stats: HomeStats = HomeStats(),
    val syncStatus: SyncStatusUi = SyncStatusUi.Idle,
    val syncError: String? = null,
)

/**
 * Shared (KMP) ViewModel for Home. The session list and planned session come from DB Flows; sync
 * status is a separate local flow. All three are combined into one [StateFlow]. The UI observes
 * the DB, never the network — the sync engine only feeds the DB.
 */
class HomeViewModel(
    private val repository: SessionRepository,
    private val syncEngine: SyncEngine,
) : ViewModel() {

    private val syncStatus = MutableStateFlow(SyncStatusUi.Idle)
    private val syncError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(
            repository.observeSessions(),
            repository.observePlannedSession(),
            syncStatus,
            syncError,
        ) { sessions, planned, status, error ->
            HomeUiState(
                sessions = sessions,
                plannedSession = planned,
                stats = computeStats(sessions),
                syncStatus = status,
                syncError = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    init {
        viewModelScope.launch { repository.ensureSeeded() }
        // Sync-on-open: pull anything logged on another device since last time.
        sync()
    }

    /** Intent: start the planned session. The write is local-first and instant. */
    fun onStartPlannedSession() {
        val plan = uiState.value.plannedSession ?: return
        viewModelScope.launch { repository.startPlannedSession(plan) }
    }

    /** Intent: run a foreground sync pass (push the outbox, pull remote changes). */
    fun onSyncClick() {
        sync()
    }

    private fun sync() {
        viewModelScope.launch {
            syncStatus.value = SyncStatusUi.Syncing
            when (val result = syncEngine.sync()) {
                is SyncResult.Success -> {
                    syncStatus.value = SyncStatusUi.Idle
                    syncError.value = null
                }
                is SyncResult.Failure -> {
                    syncStatus.value = SyncStatusUi.Error
                    syncError.value = result.error.message ?: "Sync failed"
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun computeStats(sessions: List<Session>): HomeStats {
        if (sessions.isEmpty()) return HomeStats()
        val now = Clock.System.now().toEpochMilliseconds()
        val dayMs = 86_400_000L

        val last7 = sessions.count { it.startedAt >= now - 7 * dayMs }

        // Streak: consecutive UTC epoch-days with >=1 session, ending today (or yesterday if today
        // has none yet, so an active streak doesn't "break" until a full day is missed). UTC-bucketed
        // for a dependency-free v1 — precise local-timezone bucketing is a noted refinement.
        val trainedDays = sessions.map { it.startedAt / dayMs }.toSet()
        val today = now / dayMs
        var cursor = if (trainedDays.contains(today)) today else today - 1
        var streak = 0
        while (trainedDays.contains(cursor)) {
            streak++
            cursor--
        }

        return HomeStats(total = sessions.size, last7Days = last7, dayStreak = streak)
    }
}
