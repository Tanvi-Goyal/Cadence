package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.MindSetTopBar
import com.mindset.model.ActiveWorkout
import com.mindset.model.BottomNavTab
import com.mindset.presentation.HomeUiState
import com.mindset.presentation.HomeViewModel
import com.mindset.presentation.Widget
import com.mindset.presentation.WidgetSlot
import com.mindset.presentation.WidgetState
import com.mindset.presentation.WidgetType
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.viewmodel.koinViewModel

private const val HomeGridColumns = 2
private val CompactWidgets = setOf(WidgetType.RaceGoal, WidgetType.Performance)

@Composable
fun HomeScreen(
    onOpenTemplate: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    onTab: (BottomNavTab) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MindSetTheme {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                MindSetTopBar(
                    onProfileClick = { onTab(BottomNavTab.Profile) },
                )
            },
            bottomBar = {
                BottomNavBar(current = BottomNavTab.Home, onTabClick = onTab)
            },
        ) { padding ->
            HomeContent(
                state = state,
                activeWorkout = viewModel.activeWorkout,
                onOpenDetail = onOpenDetail,
                onOpenTemplate = onOpenTemplate,
                onSeeAll = onSeeAll,
                onExpandWorkout = viewModel::onExpandWorkout,
                onResetWorkout = viewModel::onResetWorkout,
                onToggleWorkoutPause = viewModel::onToggleWorkoutPause,
                onAdvanceWorkout = viewModel::onAdvanceWorkout,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    activeWorkout: StateFlow<ActiveWorkout?>,
    onOpenDetail: (String) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onSeeAll: () -> Unit,
    onExpandWorkout: () -> Unit,
    onResetWorkout: () -> Unit,
    onToggleWorkoutPause: () -> Unit,
    onAdvanceWorkout: () -> Unit,
    contentPadding: PaddingValues,
) {
    val spacing = MaterialTheme.spacing

    LazyVerticalGrid(
        columns = GridCells.Fixed(HomeGridColumns),
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = spacing.md,
            end = spacing.md,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.md,
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.smd),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        items(
            state.widgets,
            key = { it.type },
            span = { slot ->
                if (slot.type in CompactWidgets)
                    GridItemSpan(1)
                else GridItemSpan(maxLineSpan)
            },
        ) { slot ->
            WidgetSlotContent(
                slot = slot,
                activeWorkout = activeWorkout,
                onOpenDetail = onOpenDetail,
                onOpenTemplate = onOpenTemplate,
                onSeeAll = onSeeAll,
                onExpandWorkout = onExpandWorkout,
                onResetWorkout = onResetWorkout,
                onToggleWorkoutPause = onToggleWorkoutPause,
                onAdvanceWorkout = onAdvanceWorkout,
            )
        }
    }
}

@Composable
private fun WidgetSlotContent(
    slot: WidgetSlot,
    activeWorkout: StateFlow<ActiveWorkout?>,
    onOpenDetail: (String) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onSeeAll: () -> Unit,
    onExpandWorkout: () -> Unit,
    onResetWorkout: () -> Unit,
    onToggleWorkoutPause: () -> Unit,
    onAdvanceWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val widgetState = slot.state) {
        WidgetState.Loading -> Unit
        is WidgetState.Error -> EmptyHomePlaceholder(
            widgetState.message,
            modifier.fillMaxWidth(),
        )

        is WidgetState.Content -> when (val widget = widgetState.widget) {
            is Widget.RaceGoalWidget ->
                RaceGoalCard(widget, modifier)

            is Widget.PerformanceWidget ->
                WeeklyPerformanceCard(widget, modifier)

            is Widget.SimulationWidget ->
                SimulationCard(widget, onOpenTemplate, modifier)

            is Widget.RecentSessionsWidget ->
                RecentSessionsCard(widget, onOpenDetail, onSeeAll, modifier)

            Widget.LiveWorkoutWidget ->
                LiveWorkoutSlot(
                    activeWorkout = activeWorkout,
                    onExpand = onExpandWorkout,
                    onReset = onResetWorkout,
                    onTogglePause = onToggleWorkoutPause,
                    onNext = onAdvanceWorkout,
                    modifier = modifier,
                )
        }
    }
}
