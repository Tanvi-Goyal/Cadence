package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.ActiveWorkout
import dev.cadence.domain.ActiveWorkoutController
import dev.cadence.domain.SessionRepository
import dev.cadence.domain.personalBestSessionIds
import dev.cadence.domain.trainingStreakDays
import dev.cadence.model.PlannedSession
import dev.cadence.model.Session
import dev.cadence.domain.SyncOutcome
import dev.cadence.domain.Syncer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Where a sync pass currently stands, for the UI's sync indicator. */
enum class SyncStatusUi { Idle, Syncing, Error }

/** Home summary stats derived from the session list + logged-set volumes. */
data class HomeStats(
    val total: Int = 0,
    val dayStreak: Int = 0,
    val totalVolumeKg: Double = 0.0,
)

/** Immutable UI state for the Home screen. */
data class HomeUiState(
    val sessions: List<Session> = emptyList(),
    val plannedSession: PlannedSession? = null,
    val stats: HomeStats = HomeStats(),
    val volumeBySession: Map<String, Double> = emptyMap(),
    /** The user's templates, surfaced as Home quick-start chips. */
    val templates: List<Session> = emptyList(),
    /** Session ids that were a personal best (new all-time volume high) — drive the Recent "PB" tag. */
    val pbSessionIds: Set<String> = emptySet(),
    val syncStatus: SyncStatusUi = SyncStatusUi.Idle,
    val syncError: String? = null,
)

/** The four DB-derived inputs to Home, combined before the sync flows are folded in. */
private data class HomeData(
    val sessions: List<Session>,
    val planned: PlannedSession?,
    val volumes: Map<String, Double>,
    val templates: List<Session>,
)

/**
 * Shared (KMP) ViewModel for Home. The session list and planned session come from DB Flows; sync
 * status is a separate local flow. All three are combined into one [StateFlow]. The UI observes
 * the DB, never the network — the sync engine only feeds the DB.
 */
class HomeViewModel(
    private val repository: SessionRepository,
    private val syncer: Syncer,
    private val activeWorkoutController: ActiveWorkoutController,
) : ViewModel() {

    private val syncStatus = MutableStateFlow(SyncStatusUi.Idle)
    private val syncError = MutableStateFlow<String?>(null)

    /**
     * The live HYROX workout (or null). Deliberately exposed as its OWN StateFlow — NOT folded into
     * [uiState] — so its ~5 Hz tick recomposes only the Home timer card, never the whole Home state or
     * its sibling sections. Pass-through of the app-scoped controller's state.
     */
    val activeWorkout: StateFlow<ActiveWorkout?> get() = activeWorkoutController.state

    /** One-shot navigation: emits the id of a session to open in Log Workout. */
    private val _openSession = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val openSession: SharedFlow<String> = _openSession.asSharedFlow()

    // The four DB streams are combined first (typed combine tops out at 5 args); the two sync flows
    // are folded in a second combine so we stay within arity and keep the mapping readable.
    private val homeData: Flow<HomeData> =
        combine(
            repository.observeSessions(),
            repository.observePlannedSession(),
            repository.observeVolumesBySession(),
            repository.observeTemplates(),
        ) { sessions, planned, volumes, templates ->
            HomeData(sessions, planned, volumes, templates)
        }

    val uiState: StateFlow<HomeUiState> =
        combine(homeData, syncStatus, syncError) { data, status, error ->
            HomeUiState(
                sessions = data.sessions,
                plannedSession = data.planned,
                stats = computeStats(data.sessions, data.volumes.values.sum()),
                volumeBySession = data.volumes,
                templates = data.templates,
                pbSessionIds = personalBestSessionIds(data.sessions, data.volumes),
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

    /** Intent: start the planned session, then navigate into Log Workout on the new session. */
    fun onStartPlannedSession() {
        val plan = uiState.value.plannedSession ?: return
        viewModelScope.launch {
            val session = repository.startPlannedSession(plan)
            _openSession.tryEmit(session.id)
        }
    }

    /** Intent: run a foreground sync pass (push the outbox, pull remote changes). */
    fun onSyncClick() {
        sync()
    }

    /** Intent: re-open the full timer sheet from the Home mini-card. */
    fun onExpandWorkout() {
        activeWorkoutController.expand()
    }

    /** Intent: restart the live workout from the first step (stays on whichever surface is showing). */
    fun onResetWorkout() {
        activeWorkoutController.reset()
    }

    /** Intent: toggle pause/resume on the live workout. */
    fun onToggleWorkoutPause() {
        val workout = activeWorkoutController.state.value ?: return
        if (workout.paused) activeWorkoutController.resume() else activeWorkoutController.pause()
    }

    /**
     * Intent: complete the current step and advance — or finish if it was the last. Finishing from the
     * minimized Home card re-expands the sheet so the completion summary is surfaced.
     */
    fun onAdvanceWorkout() {
        val workout = activeWorkoutController.state.value ?: return
        val wasLast = workout.currentIndex >= workout.totalSteps - 1
        activeWorkoutController.next()
        if (wasLast) activeWorkoutController.expand()
    }

    private fun sync() {
        viewModelScope.launch {
            syncStatus.value = SyncStatusUi.Syncing
            when (val result = syncer.sync()) {
                is SyncOutcome.Success -> {
                    syncStatus.value = SyncStatusUi.Idle
                    syncError.value = null
                }
                is SyncOutcome.Failure -> {
                    syncStatus.value = SyncStatusUi.Error
                    syncError.value = result.error.message ?: "Sync failed"
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun computeStats(sessions: List<Session>, totalVolumeKg: Double): HomeStats {
        if (sessions.isEmpty()) return HomeStats(totalVolumeKg = totalVolumeKg)
        val now = Clock.System.now().toEpochMilliseconds()
        val streak = trainingStreakDays(sessions.map { it.startedAt.toEpochMilliseconds() }, now)
        return HomeStats(total = sessions.size, dayStreak = streak, totalVolumeKg = totalVolumeKg)
    }
}
