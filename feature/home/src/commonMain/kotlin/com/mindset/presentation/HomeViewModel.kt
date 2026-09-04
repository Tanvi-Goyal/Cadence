package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.RaceGoalRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.RaceGoal
import com.mindset.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val DAY_MS = 86_400_000L

@OptIn(ExperimentalTime::class)
class HomeViewModel(
    private val repository: SessionRepository,
    private val activeWorkoutController: ActiveWorkoutController,
    private val raceGoalRepository: RaceGoalRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        raceGoalSlot(),
        performanceSlot(),
        recentSessionsSlot(),
    ) { race, performance, recent ->
        // BrowseTemplates is a static entry point (no data source), so it's added directly, not via a flow.
        val browse = slot(WidgetType.BrowseTemplates, Widget.BrowseTemplatesWidget)
        val bySlotType = (listOfNotNull(race, performance, recent) + browse).associateBy { it.type }
        HomeUiState(widgets = WidgetOrder.Default.types.mapNotNull { bySlotType[it] })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private fun raceGoalSlot(): Flow<WidgetSlot?> =
        raceGoalRepository.observeUpcoming()
            .map { goal ->
                goal?.let { slot(WidgetType.RaceGoal, buildRaceGoalWidget(it)) }
            }
            .onStart { emit(loading(WidgetType.RaceGoal)) }
            .catch { emit(error(WidgetType.RaceGoal, "Couldn't load race goal")) }

    // Both "This Week" variants (the current design + the TEMP A/B tick variant) derive from the same
    // bounded weekly read — [build] just picks which Widget the week data becomes.
    private fun performanceSlot() = performanceLikeSlot(WidgetType.Performance) { count, trained, today ->
        Widget.PerformanceWidget(count, trained, today)
    }

    // Bounded to the current Mon–Sun window so it reads only this week's rows. The window start is fixed
    // when the flow is built; day states are derived from UTC epoch-days, matching History.
    private fun performanceLikeSlot(
        type: WidgetType,
        build: (sessionCount: Int, trainedEpochDays: Set<Long>, todayEpochDay: Long) -> Widget,
    ): Flow<WidgetSlot?> {
        val todayEpochDay = Clock.System.now().toEpochMilliseconds() / DAY_MS
        val mondayIndex = (((todayEpochDay % 7) + 3) % 7) // epoch-day 0 is a Thursday → Monday index 3
        val currentMonday = todayEpochDay - mondayIndex
        return repository.observeSessionsSince(currentMonday * DAY_MS)
            .map<List<Session>, WidgetSlot?> { sessions ->
                val trained = sessions
                    .map { it.startedAt.toEpochMilliseconds() / DAY_MS }
                    .filter { it >= currentMonday } // guard if the window start drifts past a week boundary
                    .toSet()
                slot(type, build(trained.size, trained, todayEpochDay))
            }
            .onStart { emit(loading(type)) }
            .catch { emit(error(type, "Couldn't load week")) }
    }

    private fun recentSessionsSlot(): Flow<WidgetSlot?> =
        combine(
            repository.observeRecentSessions(5),
            repository.observeVolumesBySession(),
            repository.observeDurationsBySession(),
        ) { sessions, volumes, durations ->
            slot(
                WidgetType.RecentSessions,
                Widget.RecentSessionsWidget(sessions, volumes, durations),
            )
        }
            .onStart { emit(loading(WidgetType.RecentSessions)) }
            .catch { emit(error(WidgetType.RecentSessions, "Couldn't load sessions")) }

    private fun buildRaceGoalWidget(goal: RaceGoal): Widget.RaceGoalWidget {
        val daysUntil = goal.targetDate?.let { target ->
            val diffDays = (target.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()) / DAY_MS
            diffDays.toInt().coerceAtLeast(0)
        }
        val title = listOfNotNull(goal.formatKey, goal.city).joinToString(" · ")
        val subtitle = listOfNotNull(
            goal.divisionKey.lowercase().replaceFirstChar { it.uppercase() },
            goal.mode.name.lowercase().replaceFirstChar { it.uppercase() },
        ).joinToString(" · ")

        return Widget.RaceGoalWidget(title = title, subtitle = subtitle, daysUntil = daysUntil)
    }

    private fun slot(
        type: WidgetType,
        widget: Widget,
    ) = WidgetSlot(type, WidgetState.Content(widget))

    private fun loading(type: WidgetType) = WidgetSlot(type, WidgetState.Loading)
    private fun error(type: WidgetType, message: String) = WidgetSlot(type, WidgetState.Error(message))

    fun onExpandWorkout() {
        activeWorkoutController.expand()
    }

    fun onResetWorkout() {
        activeWorkoutController.reset()
    }

    fun onToggleWorkoutPause() {
        val workout = activeWorkoutController.state.value ?: return
        if (workout.paused) activeWorkoutController.resume() else activeWorkoutController.pause()
    }

    fun onAdvanceWorkout() {
        val workout = activeWorkoutController.state.value ?: return
        val wasLast = workout.currentIndex >= workout.totalSteps - 1
        activeWorkoutController.next()
        if (wasLast) activeWorkoutController.expand()
    }
}
