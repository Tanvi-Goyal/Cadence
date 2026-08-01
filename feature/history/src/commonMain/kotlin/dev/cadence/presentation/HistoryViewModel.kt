@file:OptIn(ExperimentalTime::class)

package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.SessionRepository
import dev.cadence.domain.personalBestSessionIds
import dev.cadence.domain.trainingStreakDays
import dev.cadence.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** A history row: the session plus its computed volume. */
data class HistoryRow(
    val session: Session,
    val volumeKg: Double,
)

/** The workout-type filter behind the History header's filter control. */
enum class HistoryFilter { ALL, STRENGTH, CONDITIONING, HYROX, MIXED }

/** Immutable UI state for the History tab. */
data class HistoryUiState(
    /** The feed, already narrowed by [filter]. */
    val rows: List<HistoryRow> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.ALL,
    val streakDays: Int = 0,
    /** UTC epoch-days that had ≥1 session — drives the week-strip highlighting. */
    val trainedEpochDays: Set<Long> = emptySet(),
    val todayEpochDay: Long = 0,
    /** The most-recent personal-best session (unfiltered), spotlighted as the best-effort card. */
    val bestEffort: HistoryRow? = null,
)

private const val DAY_MS = 86_400_000L

/**
 * Backs the History tab — reactive from the DB (never the network). Reuses the same session + volume
 * flows Home uses, plus a workout-type filter, the shared training-streak computation, and the shared
 * personal-best detection for the best-effort spotlight.
 */
class HistoryViewModel(
    repository: SessionRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(HistoryFilter.ALL)

    val uiState: StateFlow<HistoryUiState> =
        combine(
            repository.observeSessions(),
            repository.observeVolumesBySession(),
            filter,
        ) { sessions, volumes, activeFilter ->
            val now = Clock.System.now().toEpochMilliseconds()
            val allRows = sessions.map { HistoryRow(it, volumes[it.id] ?: 0.0) }
            val pbIds = personalBestSessionIds(sessions, volumes)
            HistoryUiState(
                rows = if (activeFilter == HistoryFilter.ALL) {
                    allRows
                } else {
                    allRows.filter { it.session.type.name == activeFilter.name }
                },
                filter = activeFilter,
                streakDays = trainingStreakDays(sessions.map { it.startedAt.toEpochMilliseconds() }, now),
                trainedEpochDays = sessions.map { it.startedAt.toEpochMilliseconds() / DAY_MS }.toSet(),
                todayEpochDay = now / DAY_MS,
                // sessions are newest-first (SessionDao ORDER BY startedAt DESC), so the first PB match
                // is the most recent one.
                bestEffort = allRows.firstOrNull { it.session.id in pbIds },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    /** Intent: narrow the feed to a workout type (or [HistoryFilter.ALL] to clear). */
    fun onFilterSelected(newFilter: HistoryFilter) {
        filter.value = newFilter
    }
}
