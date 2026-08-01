@file:OptIn(kotlin.time.ExperimentalTime::class)

package dev.cadence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.domain.Units
import dev.cadence.icons.Calendar
import dev.cadence.icons.ChevronRight
import dev.cadence.icons.Flame
import dev.cadence.icons.Tune
import dev.cadence.presentation.HistoryFilter
import dev.cadence.presentation.HistoryRow
import dev.cadence.presentation.HistoryUiState
import dev.cadence.presentation.HistoryViewModel
import dev.cadence.ui.R
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Screen-edge gutter — matches Home's 20dp (the Figma frame margin), not the 16dp grid default. */
private val ScreenGutter = 20.dp

@Composable
fun HistoryScreen(
    onOpenDetail: (String) -> Unit,
    onTab: (Tab) -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CadenceTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { CadenceBottomBar(current = Tab.History, onTab = onTab) },
        ) { padding ->
            HistoryContent(
                state = state,
                onFilterSelected = viewModel::onFilterSelected,
                onOpenDetail = onOpenDetail,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    onFilterSelected: (HistoryFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    val spacing = MaterialTheme.spacing
    // Build the interleaved feed: the best-effort spotlight is pulled OUT of the normal rows (so it
    // never appears twice) and re-inserted after the 3rd card, matching the Figma. Only shown unfiltered.
    val hero = state.bestEffort.takeIf { state.filter == HistoryFilter.ALL }
    val feed = remember(state.rows, hero) {
        val rows = if (hero != null) state.rows.filter { it.session.id != hero.session.id } else state.rows
        buildList<FeedEntry> {
            rows.forEachIndexed { index, row ->
                add(FeedEntry.Workout(row))
                if (hero != null && index == 2) add(FeedEntry.Best(hero))
            }
            if (hero != null && rows.size <= 2) add(FeedEntry.Best(hero))
        }
    }

    LazyColumn(
        // Stable handle for the scroll Macrobenchmark (exposed via testTagsAsResourceId at the app root).
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("history_list"),
        contentPadding = PaddingValues(
            start = ScreenGutter,
            end = ScreenGutter,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item { HistoryHeader(state.filter, onFilterSelected) }

        item { TrainingStreakSection(state.streakDays, state.trainedEpochDays, state.todayEpochDay) }

        item { RecentWorkoutsHeader() }

        if (feed.isEmpty()) {
            item { EmptyHint("No sessions logged yet.", Modifier.fillMaxWidth()) }
        } else {
            items(feed, key = { it.key }) { entry ->
                when (entry) {
                    is FeedEntry.Workout -> WorkoutFeedCard(entry.row) { onOpenDetail(entry.row.session.id) }
                    is FeedEntry.Best -> BestEffortCard(entry.row) { onOpenDetail(entry.row.session.id) }
                }
            }
        }
    }
}

/** One entry in the History feed — a normal workout card or the best-effort spotlight. */
private sealed interface FeedEntry {
    val key: String
    data class Workout(val row: HistoryRow) : FeedEntry {
        override val key get() = row.session.id
    }
    data class Best(val row: HistoryRow) : FeedEntry {
        override val key get() = "best-" + row.session.id
    }
}

@Composable
private fun HistoryHeader(filter: HistoryFilter, onFilterSelected: (HistoryFilter) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            FilterButton(filter, onFilterSelected)
            // Calendar: visual-only placeholder this pass (a month/date view is a later pass).
            HeaderIconButton(icon = CadenceIcons.Calendar, contentDescription = "Calendar", tint = colors.primary, onClick = {})
        }
    }
}

@Composable
private fun FilterButton(filter: HistoryFilter, onFilterSelected: (HistoryFilter) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Box {
        HeaderIconButton(
            icon = CadenceIcons.Tune,
            contentDescription = "Filter",
            // Accent the filter glyph while a non-ALL filter is active.
            tint = if (filter == HistoryFilter.ALL) colors.primary else colors.onSurface,
            onClick = { open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            HistoryFilter.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(filterLabel(option), color = if (option == filter) colors.primary else colors.onSurface) },
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
        modifier = Modifier
            .clip(CircleShape)
            .size(24.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TrainingStreakSection(streakDays: Int, trainedEpochDays: Set<Long>, todayEpochDay: Long) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        SectionLabel("Training streak")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                Text(
                    text = "$streakDays Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Icon(CadenceIcons.Flame, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            Text(
                text = currentMonthLabel(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerLow)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
                .padding(MaterialTheme.spacing.md),
        ) {
            WeekStrip(todayEpochDay, trainedEpochDays)
        }
    }
}

@Composable
private fun WeekStrip(todayEpochDay: Long, trained: Set<Long>) {
    val cells = remember(todayEpochDay, trained) { buildWeekCells(todayEpochDay, trained) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        cells.forEach { cell -> DayCellView(cell) }
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
            text = cell.letter,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        val base = Modifier.size(36.dp).clip(MaterialTheme.shapes.small)
        val boxModifier = when (cell.state) {
            DayState.TODAY -> base.background(colors.primary)
            DayState.TRAINED -> base.border(1.dp, colors.primary, MaterialTheme.shapes.small)
            DayState.IDLE -> base
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

@Composable
private fun RecentWorkoutsHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        SectionLabel("Recent workouts")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}

@Composable
private fun WorkoutFeedCard(row: HistoryRow, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        IconMedallion(icon = typeIcon(row.session.type.name))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                text = row.session.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
            CadenceIcons.ChevronRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun BestEffortCard(row: HistoryRow, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.template_heavy_squat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, colors.background))),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                text = "Best Effort Session",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
            Text(
                text = row.session.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── metric + calendar formatting (Android layer, mirrors relativeDate's approach) ──────────────────

/** Strength → "12,450 kg vol." (grouped, full number); timed workout → total duration; else "—". */
private fun workoutMetric(row: HistoryRow, unit: dev.cadence.domain.WeightUnit): String {
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
private data class WeekCell(val letter: String, val dayOfMonth: String, val state: DayState)

// UTC-timezone'd formatters so day derivation matches the UTC epoch-day buckets used for streak/trained.
private val utcWeekdayFormat = SimpleDateFormat("EEEEE", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
private val utcDayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }

private fun buildWeekCells(todayEpochDay: Long, trained: Set<Long>): List<WeekCell> {
    // epoch-day 0 (1970-01-01) is a Thursday → Monday-based index 3.
    val mondayIndex = (((todayEpochDay % 7) + 3) % 7).toInt()
    val monday = todayEpochDay - mondayIndex
    return (0..6).map { offset ->
        val day = monday + offset
        val date = Date(day * 86_400_000L)
        val state = when {
            day == todayEpochDay -> DayState.TODAY
            day in trained -> DayState.TRAINED
            else -> DayState.IDLE
        }
        WeekCell(utcWeekdayFormat.format(date), utcDayOfMonthFormat.format(date), state)
    }
}

private fun currentMonthLabel(): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
