@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.mindset

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mindset.components.MindSetTopBar
import com.mindset.components.PrimaryButton
import com.mindset.domain.Units
import com.mindset.icons.ChevronRight
import com.mindset.icons.Flame
import com.mindset.icons.Lock
import com.mindset.icons.Tune
import com.mindset.presentation.HistoryFilter
import com.mindset.presentation.HistoryRow
import com.mindset.presentation.HistoryUiState
import com.mindset.presentation.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryScreen(
    onOpenDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onTab: (Tab) -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { MindSetTopBar(onProfileClick = onOpenProfile) },
//            bottomBar = { MindSetBottomBar(current = Tab.History, onTab = onTab) },
        ) { padding ->
            if (state.isPro) {
                ProHistory(state, viewModel, onOpenDetail, padding)
            } else {
                FreeHistory(state, onOpenDetail, padding)
            }
        }
    }
}

/** Screen edge padding — the app standard (16dp) on all four sides, plus the scaffold insets. */
@Composable
private fun screenPadding(inset: PaddingValues): PaddingValues {
    val md = MaterialTheme.spacing.md
    return PaddingValues(
        start = md,
        end = md,
        top = inset.calculateTopPadding() + md,
        bottom = inset.calculateBottomPadding() + md,
    )
}

// ── Free tier ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun FreeHistory(
    state: HistoryUiState,
    onOpenDetail: (String) -> Unit,
    inset: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .testTag("history_list"),
        contentPadding = screenPadding(inset),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        item { PerformanceHistoryHeader("Review your mental and physical data logs.") }
        item { BentoStreakCalendar(state.trainedEpochDays, state.todayEpochDay) }
        item { SectionHeader("Recent workouts (30 days)") }
        if (state.freeRows.isEmpty()) {
            item { EmptyHint("No sessions logged yet.", Modifier.fillMaxWidth()) }
        } else {
            items(state.freeRows, key = { it.session.id }) { row ->
                WorkoutFeedCard(
                    row,
                    isPb = row.session.id in state.pbSessionIds
                ) { onOpenDetail(row.session.id) }
            }
        }
        item { PaywallCard() }
    }
}

// ── Pro tier ──────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProHistory(
    state: HistoryUiState,
    viewModel: HistoryViewModel,
    onOpenDetail: (String) -> Unit,
    inset: PaddingValues,
) {
    val paged = viewModel.pagedSessions.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .testTag("history_list"),
        contentPadding = screenPadding(inset),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        item { PerformanceHistoryHeader("Unlimited Pro Tier access to your training evolution.") }
        item { ProStreakStrip(state) }
        item { FilterRow(state.filter, viewModel::onFilterSelected) }

        items(paged.itemCount, key = paged.itemKey { it.id }) { index ->
            val session = paged[index] ?: return@items
            WorkoutFeedCard(
                row = HistoryRow(session, state.volumes[session.id] ?: 0.0),
                isPb = session.id in state.pbSessionIds,
            ) { onOpenDetail(session.id) }
        }

        if (paged.loadState.append is LoadState.Loading) {
            item { LoadingRow() }
        }
        if (paged.itemCount == 0 && paged.loadState.refresh !is LoadState.Loading) {
            item { EmptyHint("No sessions logged yet.", Modifier.fillMaxWidth()) }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun PerformanceHistoryHeader(subtitle: String) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        Text(
            text = "Performance History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeader(label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        SectionLabel(label)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}

@Composable
private fun FilterRow(filter: HistoryFilter, onFilterSelected: (HistoryFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionLabel("All sessions")
        FilterButton(filter, onFilterSelected)
    }
}

// ── Filter control (reused) ─────────────────────────────────────────────────────────────────────

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
                            color = if (option == filter) colors.primary else colors.onSurface
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
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clip(CircleShape).size(24.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Streak calendars ────────────────────────────────────────────────────────────────────────────

/** Free tier: a 7-wide × 4-week bento grid inside a glass card. */
@Composable
private fun BentoStreakCalendar(trained: Set<Long>, todayEpochDay: Long) {
    val colors = MaterialTheme.colorScheme
    val weeks =
        remember(trained, todayEpochDay) { buildCalendarWeeks(todayEpochDay, trained, weeks = 4) }
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        StreakHeaderRow()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerLow)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
                .padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            weeks.forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    week.forEach { cell -> DayCellView(cell) }
                }
            }
        }
    }
}

@Composable
private fun StreakHeaderRow() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionLabel("Training streak")
        Text(
            text = currentMonthLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
        )
    }
}

/** Pro tier: streak + best-streak stats over a horizontally-scrollable strip of recent days. */
@Composable
private fun ProStreakStrip(state: HistoryUiState) {
    val colors = MaterialTheme.colorScheme
    val days = remember(state.trainedEpochDays, state.todayEpochDay) {
        buildDayCells(state.todayEpochDay, state.trainedEpochDays, count = 21)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
            .padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            StreakStat(
                value = state.streakDays.toString(),
                label = "Day streak",
                icon = MindSetIcons.Flame
            )
            StreakStat(value = state.longestStreakDays.toString(), label = "Best", alignEnd = true)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            items(days, key = { it.epochDay }) { cell -> DayCellView(cell) }
        }
    }
}

@Composable
private fun StreakStat(
    value: String,
    label: String,
    icon: ImageVector? = null,
    alignEnd: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            if (icon != null) Icon(
                icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DayCellView(cell: WeekCell) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Text(
            cell.letter,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
        val base = Modifier.size(36.dp).clip(MaterialTheme.shapes.small)
        val boxModifier = when (cell.state) {
            DayState.TODAY -> base.background(colors.primary)
            DayState.TRAINED -> base.border(1.dp, colors.primary, MaterialTheme.shapes.small)
            DayState.IDLE -> base.background(GlassFill)
        }
        Box(boxModifier, contentAlignment = Alignment.Center) {
            Text(
                text = cell.dayOfMonth,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (cell.state == DayState.TODAY) FontWeight.Bold else FontWeight.Normal,
                color = when (cell.state) {
                    DayState.TODAY -> colors.onPrimary
                    DayState.TRAINED -> colors.onSurface
                    DayState.IDLE -> colors.onSurfaceVariant
                },
            )
        }
    }
}

// ── Workout row (reused across tiers) ───────────────────────────────────────────────────────────

@Composable
private fun WorkoutFeedCard(row: HistoryRow, isPb: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        IconMedallion(icon = typeIcon(row.session.type.name))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
            ) {
                Text(
                    text = row.session.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isPb) PbTag()
            }
            Text(
                text = workoutMetric(row, unit),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.primary,
            )
        }
        Text(
            text = relativeDate(row.session.startedAt.toEpochMilliseconds()),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        Icon(
            MindSetIcons.ChevronRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PbTag() {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.clip(CircleShape).background(colors.primary.copy(alpha = 0.15f))
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            "PB",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
    }
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

// ── Paywall (free tier) ─────────────────────────────────────────────────────────────────────────

@Composable
private fun PaywallCard() {
    val colors = MaterialTheme.colorScheme
    // Slow-pulsing glow behind the card (mirrors the onboarding radial-gradient backdrop idiom).
    val transition = rememberInfiniteTransition(label = "paywall-glow")
    val glow by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow-alpha",
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        listOf(
                            colors.primary.copy(alpha = glow),
                            Color.Transparent
                        )
                    )
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(GlassFill)
                .border(1.dp, GlassBorder, MaterialTheme.shapes.large)
                .padding(MaterialTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    MindSetIcons.Lock,
                    contentDescription = null,
                    tint = ObsidianCoral,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Unlock full history",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Text(
                text = "Go Pro for unlimited history, filters, and your complete training evolution.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton(
                text = "Unlock Pro — Coming Soon",
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "In-app purchases arrive in a future update.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

// ── metric + calendar formatting ────────────────────────────────────────────────────────────────

/** Strength → "12,450 kg vol." (grouped, full number); timed workout → total duration; else "—". */
private fun workoutMetric(row: HistoryRow, unit: com.mindset.domain.WeightUnit): String {
    if (row.volumeKg > 0.0) {
        val display = Units.toDisplay(row.volumeKg, unit).toInt()
        return "${String.format(Locale.getDefault(), "%,d", display)} ${Units.label(unit)} vol."
    }
    val finished = row.session.finishedAt
    return if (finished != null) {
        formatDuration(finished.toEpochMilliseconds() - row.session.startedAt.toEpochMilliseconds())
    } else {
        "—"
    }
}

private enum class DayState { TODAY, TRAINED, IDLE }
private data class WeekCell(
    val epochDay: Long,
    val letter: String,
    val dayOfMonth: String,
    val state: DayState
)

private const val DAY_MS = 86_400_000L

// UTC-timezone'd formatters so day derivation matches the UTC epoch-day buckets used for streak/trained.
private val utcWeekdayFormat =
    SimpleDateFormat("EEEEE", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
private val utcDayOfMonthFormat =
    SimpleDateFormat("d", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }

private fun cellFor(day: Long, today: Long, trained: Set<Long>): WeekCell {
    val date = Date(day * DAY_MS)
    val state = when {
        day == today -> DayState.TODAY
        day in trained -> DayState.TRAINED
        else -> DayState.IDLE
    }
    return WeekCell(day, utcWeekdayFormat.format(date), utcDayOfMonthFormat.format(date), state)
}

/** [weeks] Monday-based rows, oldest first (top) → the week containing today last (bottom). */
private fun buildCalendarWeeks(
    todayEpochDay: Long,
    trained: Set<Long>,
    weeks: Int
): List<List<WeekCell>> {
    // epoch-day 0 (1970-01-01) is a Thursday → Monday-based index 3.
    val mondayIndex = (((todayEpochDay % 7) + 3) % 7).toInt()
    val currentMonday = todayEpochDay - mondayIndex
    return (weeks - 1 downTo 0).map { w ->
        val monday = currentMonday - w * 7
        (0..6).map { offset -> cellFor(monday + offset, todayEpochDay, trained) }
    }
}

/** The last [count] days, oldest → today, for the pro horizontal strip. */
private fun buildDayCells(todayEpochDay: Long, trained: Set<Long>, count: Int): List<WeekCell> =
    ((count - 1) downTo 0).map { back -> cellFor(todayEpochDay - back, todayEpochDay, trained) }

private fun currentMonthLabel(): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
