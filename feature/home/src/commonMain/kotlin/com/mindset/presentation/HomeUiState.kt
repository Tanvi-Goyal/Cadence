package com.mindset.presentation

import androidx.compose.runtime.Immutable
import com.mindset.model.Session

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
    RaceGoal,
    Performance,
    BrowseTemplates,
    RecentSessions,
    LiveWorkout,
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
                // A live race outranks everything: it is the only widget the athlete is mid-way
                // through. Absent one, the VM's mapNotNull drops it and this list is unchanged.
                WidgetType.LiveWorkout,
                WidgetType.RaceGoal,
                WidgetType.Performance,
                WidgetType.BrowseTemplates,
                WidgetType.RecentSessions,
            ),
        )
    }
}

/**
 * A render-ready widget — its data already derived from domain in the ViewModel (primitives + read-only
 * collections only), so composables never touch unstable domain types. [type] links each variant back
 * to its slot so ordering/keys can be derived uniformly.
 */
@Immutable
sealed interface Widget {
    val type: WidgetType

    /** Countdown card: [daysUntil] to [title]'s race (null = no fixed race day). */
    @Immutable
    data class RaceGoalWidget(
        val title: String,
        val subtitle: String,
        val daysUntil: Int?,
    ) : Widget {
        override val type: WidgetType get() = WidgetType.RaceGoal
    }

    /** "This Week": [sessionCount] this week + the Mon–Sun grid derived from [trainedEpochDays]. */
    @Immutable
    data class PerformanceWidget(
        val sessionCount: Int,
        val trainedEpochDays: Set<Long>,
        val todayEpochDay: Long,
    ) : Widget {
        override val type: WidgetType get() = WidgetType.Performance
    }

    /** Static entry point into the Templates library — carries no data, just routes on tap. */
    @Immutable
    data object BrowseTemplatesWidget : Widget {
        override val type: WidgetType get() = WidgetType.BrowseTemplates
    }

    /**
     * Recent sessions preview. Carries the domain [Session]s (immutable data holders, rebuilt per
     * emission) plus their strength volume keyed by id — the row renderer reads both.
     */
    @Immutable
    data class RecentSessionsWidget(
        val sessions: List<Session>,
        val volumesById: Map<String, Double>,
        /** Training time (Σ logged splits, runs included) per session id; absent = nothing timed. */
        val durationsById: Map<String, Int>,
    ) : Widget {
        override val type: WidgetType get() = WidgetType.RecentSessions
    }

    /**
     * Self-sourcing marker — carries no data. Its Composable collects the live workout StateFlow itself
     * so the ~200 ms tick recomposes only that card and never rebuilds this widget list. The slot that
     * emits it reduces the controller flow to a presence Boolean first, so this widget appears and
     * disappears exactly twice per race rather than five times a second.
     */
    @Immutable
    data object LiveWorkoutWidget : Widget {
        override val type: WidgetType get() = WidgetType.LiveWorkout
    }
}

/** Per-widget state: independent so one widget's failure or loading never blanks the others. */
@Immutable
sealed interface WidgetState {
    data object Loading : WidgetState
    @Immutable
    data class Content(val widget: Widget) : WidgetState
    @Immutable
    data class Error(val message: String) : WidgetState
}
