package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.LocalQuickStart
import com.mindset.components.MindSetTopBar
import com.mindset.components.QuickStartFab
import com.mindset.domain.ActiveWorkout
import com.mindset.icons.Add
import com.mindset.icons.Bolt
import com.mindset.icons.ChevronRight
import com.mindset.icons.Grid
import com.mindset.model.BottomNavTab
import com.mindset.model.PlannedSession
import com.mindset.model.Session
import com.mindset.presentation.HomeUiState
import com.mindset.presentation.HomeViewModel
import kotlinx.coroutines.flow.StateFlow
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
                onExpandWorkout = viewModel::onExpandWorkout,
                onNewSession = onNewSession,
                onOpenTemplates = onOpenTemplates,
                onOpenTemplate = onOpenTemplate,
                onOpenDetail = onOpenDetail,
                onSeeAll = onSeeAll,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onExpandWorkout: () -> Unit,
    onNewSession: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenTemplate: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
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
        // When a HYROX workout is live, the timer card takes the "Up next" slot; otherwise the planned
        // session (if any) shows there. This slot collects the ticking state internally so the tick
        // recomposes only the card, not the rest of Home.
//        item {
//            UpNextOrLiveSlot(
//                activeWorkout = activeWorkout,
//                plannedSession = state.plannedSession,
//                onStartPlanned = onStartPlanned,
//                onExpandWorkout = onExpandWorkout,
//                onResetWorkout = onResetWorkout,
//                onToggleWorkoutPause = onToggleWorkoutPause,
//                onAdvanceWorkout = onAdvanceWorkout,
//            )
//        }

//        item { TemplatesSection(state.templates, onOpenTemplate, onOpenTemplates) }

        item {
            RecentSection(
                sessions = state.sessions,
                onOpenDetail = onOpenDetail,
                onSeeAll = onSeeAll,
            )
        }
    }
}
//
//@Composable
//private fun HomeHeader(syncStatus: SyncStatusUi, syncError: String?, onSync: () -> Unit, onNewSession: () -> Unit) {
//    val colors = MaterialTheme.colorScheme
//    Column {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Text(
//                text = "Home",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//                color = colors.onSurface,
//            )
//            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
//                HeaderIconButton(
//                    icon = MindSetIcons.Add,
//                    contentDescription = "New session",
//                    tint = colors.primary,
//                    onClick = onNewSession,
//                )
//            }
//        }
//    }
//}

@Composable
private fun HeaderIconButton(icon: ImageVector, contentDescription: String, tint: Color, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun UpNextSection(plan: PlannedSession, onStart: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        SectionLabel("Up next")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerHighest)
                .clickable(onClick = onStart)
                .padding(MaterialTheme.spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    ) {
                        Text(
                            text = plan.focus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        MetaDot()
                        Text(
                            text = "${plan.targetDurationMin} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                AccentPill(text = typeLabel(plan.type.name), icon = MindSetIcons.Bolt)
            }
        }
    }
}

/**
 * The "Up next" slot: a live HYROX timer card when a workout is running, else the planned session, else
 * nothing. Collects the ticking [activeWorkout] here (not in [HomeUiState]) so only this slot — not the
 * rest of Home — recomposes on each ~200 ms tick.
 */
@Composable
private fun UpNextOrLiveSlot(
    activeWorkout: StateFlow<ActiveWorkout?>,
    plannedSession: PlannedSession?,
    onStartPlanned: () -> Unit,
    onExpandWorkout: () -> Unit,
    onResetWorkout: () -> Unit,
    onToggleWorkoutPause: () -> Unit,
    onAdvanceWorkout: () -> Unit,
) {
    val workout by activeWorkout.collectAsStateWithLifecycle()
    val live = workout
    when {
        live != null -> LiveWorkoutCard(
            workout = live,
            onExpand = onExpandWorkout,
            onReset = onResetWorkout,
            onTogglePause = onToggleWorkoutPause,
            onNext = onAdvanceWorkout,
        )

        plannedSession != null -> UpNextSection(plannedSession, onStartPlanned)
        // else: neither a live workout nor a planned session — render nothing.
    }
}

/**
 * Compact live-workout card (echoes the timer sheet's accent). Tapping the body re-expands the full
 * sheet; the Reset / Pause / Next pills drive the same controller actions inline so the user can run
 * the workout without expanding. Controls hide once the workout is finished.
 */
@Composable
private fun LiveWorkoutCard(workout: ActiveWorkout, onExpand: () -> Unit, onReset: () -> Unit, onTogglePause: () -> Unit, onNext: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        SectionLabel("In progress")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainer)
                .border(1.dp, colors.primary.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .clickable(onClick = onExpand)
                .padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Text(
                        text = workout.current?.title ?: "Workout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SectionLabel("Step ${workout.currentIndex + 1} of ${workout.totalSteps}")
                }
                LiveClock(
                    totalMs = workout.totalElapsedMs,
                    paused = workout.paused,
                    finished = workout.finished,
                )
                Icon(
                    imageVector = MindSetIcons.ChevronRight,
                    contentDescription = "Open workout",
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (!workout.finished) {
                val isLast = workout.currentIndex >= workout.totalSteps - 1
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    WorkoutActionPill("Reset", primary = false, weight = 1f, onClick = onReset)
                    WorkoutActionPill(
                        text = if (workout.paused) "Resume" else "Pause",
                        primary = false,
                        weight = 1f,
                        onClick = onTogglePause,
                    )
                    WorkoutActionPill(
                        text = if (isLast) "Finish" else "Next",
                        primary = true,
                        weight = 1.4f,
                        onClick = onNext,
                    )
                }
            }
        }
    }
}

/** A compact pill button for the Home card's inline workout controls (mirrors the sheet's controls). */
@Composable
private fun RowScope.WorkoutActionPill(text: String, primary: Boolean, weight: Float, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val base = Modifier
        .weight(weight)
        .height(44.dp)
        .clip(CircleShape)
    val styled = if (primary) {
        base.background(colors.primary)
    } else {
        base.background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant, CircleShape)
    }
    Box(styled.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal,
            color = if (primary) colors.onPrimary else colors.onSurface,
        )
    }
}

/** The count-up clock for the Home card, using the same [formatClock] mm:ss as the timer sheet. */
@Composable
private fun LiveClock(totalMs: Long, paused: Boolean, finished: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = formatClock(totalMs),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (paused && !finished) colors.onSurfaceVariant else colors.onSurface,
        )
        Text(
            text = when {
                finished -> "DONE"
                paused -> "PAUSED"
                else -> "LIVE"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (paused && !finished) colors.error else colors.primary,
        )
    }
}

@Composable
private fun TemplatesSection(templates: List<Session>, onOpenTemplate: (String) -> Unit, onOpenTemplates: () -> Unit) {
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

@Composable
private fun RecentSection(
    sessions: List<Session>,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("Recent")
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = MaterialTheme.spacing.xs, vertical = 2.dp),
            )
        }
        if (sessions.isEmpty()) {
            EmptyHint("No sessions yet. Start your first one.", Modifier.fillMaxWidth())
        } else {
            // Grouped card: rows sit on the container surface, separated by 1dp gaps that reveal the
            // fainter backing color (matches the Figma "Overlay" grouping).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.outlineVariant.copy(alpha = 0.2f)),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                sessions.take(4).forEach { session ->
                    SessionRow(
                        session = session,
                        volumeKg = 0.0,
                        onClick = { onOpenDetail(session.id) },
                        isPb = false,
                    )
                }
            }
        }
    }
}
