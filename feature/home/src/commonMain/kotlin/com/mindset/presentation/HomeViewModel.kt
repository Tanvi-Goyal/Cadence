package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.RaceGoalRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.ActiveWorkout
import com.mindset.model.RaceGoal
import com.mindset.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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

    /**
     * The ticking live race, exposed RAW rather than folded into [HomeUiState]: the card collects this
     * itself so the ~200 ms tick invalidates only the clock, never the widget list. See [liveWorkoutSlot].
     */
    val activeWorkout: StateFlow<ActiveWorkout?> = activeWorkoutController.state

    val uiState: StateFlow<HomeUiState> = combine(
        raceGoalSlot(),
        performanceSlot(),
        recentSessionsSlot(),
        liveWorkoutSlot(),
    ) { race, performance, recent, liveWorkout ->
        // BrowseTemplates and the sim rail are static entry points (no data source), so they're added
        // directly, not via a flow.
        val browse = slot(WidgetType.BrowseTemplates, Widget.BrowseTemplatesWidget)
        val simulations = slot(WidgetType.Simulation, Widget.SimulationWidget(SIMULATIONS))
        val bySlotType = (
            listOfNotNull(race, performance, recent, liveWorkout) + browse + simulations
            ).associateBy { it.type }
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

    /**
     * Whether a live race exists — nothing more. The `map { it != null }` BEFORE
     * [distinctUntilChanged] is load-bearing: comparing [ActiveWorkout] instances would let a new
     * object through on every ~200 ms tick and rebuild the whole widget list five times a second.
     * Reduced to a Boolean it emits twice per race (start, dismiss). The card sources its own data
     * from [activeWorkout].
     */
    private fun liveWorkoutSlot(): Flow<WidgetSlot?> =
        activeWorkoutController.state
            .map { it != null }
            .distinctUntilChanged()
            .map { live -> if (live) slot(WidgetType.LiveWorkout, Widget.LiveWorkoutWidget) else null }

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

    private companion object {
        /**
         * The two race simulations, in rail order. Static for the same reason the Template Library's
         * class templates are: the sims are the fixed HYROX format, not user content. The ids are the
         * ones the nav host maps to `TemplateHyroxDetail`, and the copy matches the Library's cards so
         * the same workout doesn't read as two different things on two screens.
         */
        val SIMULATIONS = listOf(
            SimulationEntry(
                id = "full-hyrox-simulation",
                title = "Full Hyrox Simulation",
                subtitle = "8x1km Run • All 8 Stations",
                flag = "Official Sim",
                tags = listOf("Conditioning", "Run"),
                art = SimulationArt.FULL_HYROX,
            ),
            SimulationEntry(
                id = "half-hyrox-sim",
                title = "Half Hyrox Sim",
                subtitle = "4x1km • 4 Stations • Comp Pace",
                flag = "Half Sim",
                tags = listOf("Conditioning", "Pace"),
                art = SimulationArt.HALF_HYROX,
            ),
        )
    }
}
