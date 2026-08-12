package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.LocalQuickStart
import com.mindset.components.MindSetTopBar
import com.mindset.components.QuickStartFab
import com.mindset.icons.Grid
import com.mindset.model.BottomNavTab
import com.mindset.model.Session
import com.mindset.presentation.HomeUiState
import com.mindset.presentation.HomeViewModel
import com.mindset.presentation.Widget
import com.mindset.presentation.WidgetSlot
import com.mindset.presentation.WidgetState
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun HomeScreen(
    onOpenSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenTemplate: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    onTab: (BottomNavTab) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val quickStart = LocalQuickStart.current

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
            floatingActionButton = {
                QuickStartFab(onClick = quickStart)
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { padding ->

            HomeContent(
                state = state,
                onOpenDetail = onOpenDetail,
                onOpenTemplates = onOpenTemplates,
                onSeeAll = onSeeAll,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenDetail: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    onSeeAll: () -> Unit,
    contentPadding: PaddingValues,
) {
    val spacing = MaterialTheme.spacing
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = spacing.md,
            end = spacing.md,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        items(state.widgets, key = { it.type }) { slot ->
            WidgetSlotContent(
                slot = slot,
                onOpenDetail = onOpenDetail,
                onOpenTemplates = onOpenTemplates,
                onSeeAll = onSeeAll,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun WidgetSlotContent(
    slot: WidgetSlot,
    onOpenDetail: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val widgetState = slot.state) {
        WidgetState.Loading -> Unit // TODO: skeleton placeholder per widget type
        is WidgetState.Error -> EmptyHint(widgetState.message, modifier.fillMaxWidth())
        is WidgetState.Content -> when (val widget = widgetState.widget) {
            is Widget.RaceGoalWidget ->
                RaceGoalCard(widget, modifier)
            is Widget.PerformanceWidget ->
                WeeklyPerformanceCard(widget, modifier)
            is Widget.BrowseTemplatesWidget ->
                BrowseTemplatesCard(onOpenTemplates, modifier)
            is Widget.RecentSessionsWidget ->
                RecentSessionsCard(widget, onOpenDetail, onSeeAll, modifier)
            // Self-sourcing live-workout card is wired separately (collects its own tick flow).
            Widget.LiveWorkoutWidget -> Unit
        }
    }
}

@Composable
private fun TemplatesSection(
    templates: List<Session>,
    onOpenTemplate: (String) ->
    Unit,
    onOpenTemplates: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        SectionLabel("Templates")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            // Leading chip is always visible (no horizontal scroll needed) so the Templates library
            // stays reachable — it's Home's only route into it.
            MindSetChip(
                label = "Browse templates",
                icon = MindSetIcons.Grid,
                onClick = onOpenTemplates,
            )
            templates.take(6).forEach { template ->
                MindSetChip(
                    label = template.name,
                    icon = typeIcon(template.type.name),
                    onClick = { onOpenTemplate(template.id) },
                )
            }
        }
    }
}
