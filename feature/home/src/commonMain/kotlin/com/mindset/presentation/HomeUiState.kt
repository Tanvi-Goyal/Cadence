package com.mindset.presentation

import androidx.compose.runtime.Immutable
import com.mindset.model.Session
import com.mindset.presentation.WidgetOrder.Companion.Default

/**
 * Home as a declarative, ordered list of widgets. The screen renders [widgets] top-to-bottom; adding,
 * removing, or reordering a widget is a change to [WidgetOrder] (the layout) — not a change to the
 * widgets themselves.
 *
 * The whole hierarchy is `@Immutable`: each emission is rebuilt fresh and never mutated in place, so
 * the annotation is sound and lets Compose value-skip each widget — a change to one widget's data
 * leaves the others' composables un-recomposed, which keeps this LazyColumn cheap.
 *
 * Three concerns are kept separate on purpose:
 *  - [WidgetType] + [WidgetOrder] — the *layout* (which widgets, in what order). A hardcoded default
 *    today; the same shape becomes a persisted (and later synced) preference with no reshaping.
 *  - [Widget] — the *content*: the already-derived, render-ready data for one widget.
 *  - [WidgetState] — per-widget Loading/Content/Error, so one failing data source paints one card
 *    instead of blanking the whole screen.
 */
@Immutable
data class HomeUiState(
    val widgets: List<WidgetSlot> = emptyList(),
)

/**
 * One entry in the ordered Home list. Carries [type] independently of [state] because during
 * [WidgetState.Loading]/[WidgetState.Error] there is no [Widget] to read a type from — yet the slot
 * still needs a stable identity for its position and its `LazyColumn` key.
 */
@Immutable
data class WidgetSlot(
    val type: WidgetType,
    val state: WidgetState,
)

/** The identity of a widget kind — the unit the user adds/removes/reorders, and the stable list key. */
enum class WidgetType {
    LiveWorkout,
    RaceGoal,
    Performance,
    Simulation,
    RecentSessions,
}

/**
 * The Home layout: the ordered, enabled set of widgets (order = list order). Hardcoded [Default] today;
 * later this is exactly what a DataStore/DB preference stores, so "add/remove/reorder" becomes an edit
 * to this list — the widgets and their rendering don't change.
 */
data class WidgetOrder(val types: List<WidgetType>) {
    companion object {
        val Default = WidgetOrder(
            listOf(
                WidgetType.LiveWorkout,
                WidgetType.RaceGoal,
                WidgetType.Performance,
                WidgetType.Simulation,
                WidgetType.RecentSessions,
            ),
        )
    }
}

@Immutable
sealed interface Widget {
    val type: WidgetType

    @Immutable
    data object LiveWorkoutWidget : Widget {
        override val type: WidgetType get() = WidgetType.LiveWorkout
    }

    @Immutable
    data class RaceGoalWidget(
        val title: String,
        val subtitle: String,
        val daysUntil: Int?,
    ) : Widget {
        override val type: WidgetType get() = WidgetType.RaceGoal
    }

    @Immutable
    data class PerformanceWidget(
        val sessionCount: Int,
        val trainedEpochDays: Set<Long>,
        val todayEpochDay: Long,
    ) : Widget {
        override val type: WidgetType get() = WidgetType.Performance
    }

    @Immutable
    data class SimulationWidget(val sims: List<SimulationEntry>) : Widget {
        override val type: WidgetType get() = WidgetType.Simulation
    }

    @Immutable
    data class RecentSessionsWidget(
        val sessions: List<Session>,
        val volumesById: Map<String, Double>,
        /** Training time (Σ logged splits, runs included) per session id; absent = nothing timed. */
        val durationsById: Map<String, Int>,
    ) : Widget {
        override val type: WidgetType get() = WidgetType.RecentSessions
    }
}

@Immutable
data class SimulationEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val flag: String,
    val tags: List<String>,
    val type: SimulationType,
)

/**
 * Which photograph backs a sim card. A semantic enum rather than a drawable id so this stays a
 * commonMain type with no Compose/Android dependency — the screen maps each value to its resource,
 * exactly as `TemplateGlyph` is mapped to a vector.
 */
enum class SimulationType { FULL_HYROX, HALF_HYROX }

/** Per-widget state: independent so one widget's failure or loading never blanks the others. */
@Immutable
sealed interface WidgetState {
    data object Loading : WidgetState

    @Immutable
    data class Content(val widget: Widget) : WidgetState

    @Immutable
    data class Error(val message: String) : WidgetState
}
