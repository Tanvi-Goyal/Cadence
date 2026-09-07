@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mindset.components.MindSetTopBar
import com.mindset.components.SessionRow
import com.mindset.icons.Flame
import com.mindset.icons.Tune
import com.mindset.presentation.HistoryFilter
import com.mindset.presentation.HistoryUiState
import com.mindset.presentation.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * History deliberately renders with Home's vocabulary, not its own: the same glass card, the same
 * accent-tick section header, the same calendar day dot and the same grouped SessionRow list. Those
 * four pieces now live in :core:ui (glassSurface / MindSetSectionHeader / CalendarDayDot / SessionRow)
 * precisely so the two screens can't drift apart again.
 *
 * The one intentional difference is scale: Home shows the current week inside a half-width tile,
 * History shows the last 30 days across the full width.
 */

/** Days of streak history the calendar shows. 7 per row → four full rows plus a short one. */
private const val CalendarDays = 30
private const val CalendarColumns = 7

@Composable
fun HistoryScreen(
    onOpenDetail: (String) -> Unit,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MindSetTheme {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // No bottom bar: History is a push off Home now, so Back is the way out.
                MindSetTopBar(onProfileClick = onOpenProfile, onBack = onBack)
            },
        ) { padding ->
            if (state.isPro) {
                ProHistory(state, viewModel, onOpenDetail, padding)
            } else {
                FreeHistory(state, onOpenDetail, padding)
            }
        }
    }
}

@Composable
private fun listPadding(inset: PaddingValues): PaddingValues = PaddingValues(
    start = MaterialTheme.spacing.md,
    end = MaterialTheme.spacing.md,
    top = inset.calculateTopPadding() + MaterialTheme.spacing.sm,
    bottom = inset.calculateBottomPadding() + MaterialTheme.spacing.md,
)

@Composable
private fun FreeHistory(state: HistoryUiState, onOpenDetail: (String) -> Unit, inset: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("history_list"),
        contentPadding = listPadding(inset),
    ) {
        item { StreakCalendarSection(state) }
        item { SectionGap() }
        item { MindSetSectionHeader("Recent", Modifier.fillMaxWidth()) }
        item { RowGap() }

        if (state.freeRows.isEmpty()) {
            item { EmptyHint("No sessions logged yet.") }
        } else {
            items(state.freeRows, key = { it.session.id }) { row ->
                SessionRow(
                    session = row.session,
                    volumeKg = 0.0,
                    durationSec = row.durationSec,
                    onClick = { onOpenDetail(row.session.id) },
                    isPb = false,
                )
            }
        }
    }
}

@Composable
private fun ProHistory(
    state: HistoryUiState,
    viewModel: HistoryViewModel,
    onOpenDetail: (String) -> Unit,
    inset: PaddingValues,
) {
    val paged = viewModel.pagedSessions.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("history_list"),
        contentPadding = listPadding(inset),
    ) {
        item { StreakCalendarSection(state) }
        item { SectionGap() }
        item {
            MindSetSectionHeader("Recent", Modifier.fillMaxWidth()) {
                FilterButton(state.filter, viewModel::onFilterSelected)
            }
        }
        item { RowGap() }

        items(paged.itemCount, key = paged.itemKey { it.id }) { index ->
            val session = paged[index] ?: return@items
            SessionRow(
                session = session,
                volumeKg = 0.0,
                durationSec = state.durations[session.id],
                onClick = { onOpenDetail(session.id) },
                isPb = session.id in state.pbSessionIds,
            )
        }

        if (paged.loadState.append is LoadState.Loading) {
            item { LoadingRow() }
        }
        if (paged.itemCount == 0 && paged.loadState.refresh !is LoadState.Loading) {
            item { EmptyHint("No sessions logged yet.") }
        }
    }
}

/**
 * Home's "This week" card at History's scale: the same [glassSurface] and the same [CalendarDayDot],
 * over the last [CalendarDays] days laid out full width.
 *
 * The grid is plain [Row]s, not a nested LazyVerticalGrid — 30 cells is a fixed, tiny count, and a
 * lazy grid inside a LazyColumn needs a hard-coded height anyway (nested scrolling on the same axis
 * is unmeasurable). `remember` keys on the trained set so the cells are rebuilt only when it changes.
 */
@Composable
private fun StreakCalendarSection(state: HistoryUiState) {
    val days = remember(state.trainedEpochDays, state.todayEpochDay) {
        buildDayCells(state.todayEpochDay, state.trainedEpochDays, count = CalendarDays)
    }
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        MindSetSectionHeader("Last 30 days", Modifier.fillMaxWidth()) {
            Text(
                text = currentMonthLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassSurface()
                .padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                StreakStat(
                    value = state.streakDays.toString(),
                    label = "Day streak",
                    icon = MindSetIcons.Flame,
                )
                StreakStat(value = state.longestStreakDays.toString(), label = "Best", alignEnd = true)
            }
            // Explicit rows of 7 with each cell in a weighted box, rather than a FlowRow: a FlowRow's
            // `SpaceBetween` spreads the SHORT final row (30 isn't a multiple of 7) to both edges, so
            // today drifted to the far right of its own row. Weighted columns + weighted fillers keep
            // every cell under the same column at any screen width.
            days.chunked(CalendarColumns).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CalendarDayDot(cell)
                        }
                    }
                    repeat(CalendarColumns - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun StreakStat(value: String, label: String, icon: ImageVector? = null, alignEnd: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(StatIconSize),
                )
            }
        }
    }
}

private val StatIconSize = 20.dp

@Composable
private fun FilterButton(filter: HistoryFilter, onFilterSelected: (HistoryFilter) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Box {
        HeaderIconButton(
            icon = MindSetIcons.Tune,
            contentDescription = "Filter",
            tint = if (filter == HistoryFilter.ALL) colors.primary else colors.onSurface,
            onClick = { open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            HistoryFilter.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            filterLabel(option),
                            color = if (option == filter) colors.primary else colors.onSurface,
                        )
                    },
                    onClick = {
                        onFilterSelected(option)
                        open = false
                    },
                )
            }
        }
    }
}

private fun filterLabel(filter: HistoryFilter): String = when (filter) {
    HistoryFilter.ALL -> "All workouts"
    HistoryFilter.STRENGTH -> "Strength"
    HistoryFilter.CONDITIONING -> "Conditioning"
    HistoryFilter.HYROX -> "Hyrox"
    HistoryFilter.MIXED -> "Mixed"
}

@Composable
private fun HeaderIconButton(icon: ImageVector, contentDescription: String, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(CircleShape).size(HeaderIconTarget).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(HeaderIconSize),
        )
    }
}

private val HeaderIconTarget = 24.dp
private val HeaderIconSize = 18.dp

/** Between sections — the gap Home's grid puts between its widget rows. */
@Composable
private fun SectionGap() = Spacer(Modifier.size(MaterialTheme.spacing.lg))

/** Between a section header and its content. */
@Composable
private fun RowGap() = Spacer(Modifier.size(MaterialTheme.spacing.md))

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LoadingRow() {
    Text(
        text = "Loading…",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.md),
    )
}

private fun currentMonthLabel(): String = SimpleDateFormat(
    "MMMM yyyy",
    Locale.getDefault(),
).format(Date()).uppercase(Locale.getDefault())
