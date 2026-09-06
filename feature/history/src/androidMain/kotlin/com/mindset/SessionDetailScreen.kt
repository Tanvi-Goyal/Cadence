package com.mindset

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.HeaderIconButton
import com.mindset.components.formatClockMs
import com.mindset.components.formatVolume
import com.mindset.components.relativeDate
import com.mindset.components.typeLabel
import com.mindset.domain.LoggedItemUi
import com.mindset.domain.Units
import com.mindset.domain.detailSummary
import com.mindset.helpers.UIHelper
import com.mindset.icons.ArrowBack
import com.mindset.icons.ChevronRight
import com.mindset.icons.Run
import com.mindset.model.SetEntry
import com.mindset.presentation.SessionDetailUiState
import com.mindset.presentation.SessionDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val STATION_LIMIT = 4

@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        val spacing = MaterialTheme.spacing

        val numbered = remember(state.items) {
            var n = 0
            state.items.map { item ->
                val isStation = item.segmentKey?.let { "run" !in it } == true
                item to (if (isStation) ++n else null)
            }
        }
        val hasTimeline = state.items.any { it.segmentKey != null }
        val totalStations = numbered.count { it.second != null }

        val cutoff = remember(numbered) {
            var stations = 0
            var idx = numbered.size
            for (i in numbered.indices) {
                if (numbered[i].second != null && ++stations == STATION_LIMIT) {
                    idx = i + 1
                    break
                }
            }
            idx
        }
        val remaining = (totalStations - STATION_LIMIT).coerceAtLeast(0)
        var expanded by rememberSaveable(sessionId) { mutableStateOf(false) }

        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = spacing.md,
                    end = spacing.md,
                    top = padding.calculateTopPadding() + spacing.smd,
                    bottom = padding.calculateBottomPadding() + spacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                item(key = "header") { DetailHeader(state, onBack) }

                item(key = "stats") { StatRow(state) }

                item(key = "sectionLabel") {
                    MindSetSectionHeader(
                        if (hasTimeline) "Race Timeline" else "Exercises",
                        Modifier.fillMaxWidth(),
                    )
                }

                if (state.items.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "No exercises logged.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                } else if (hasTimeline) {
                    item(key = "timeline") {
                        val visible = if (expanded) numbered else numbered.take(cutoff)
                        val hasToggle = remaining > 0
                        Column {
                            visible.forEachIndexed { i, (item, station) ->
                                TimelineRow(
                                    showDot = station != null || i == 0,
                                    isFirst = i == 0,
                                    isLast = !hasToggle && i == visible.lastIndex,
                                ) {
                                    if (station != null) StationBody(item, station)
                                    else RunBody(item)
                                }
                            }
                            if (hasToggle) {
                                RemainingToggle(expanded, remaining) { expanded = !expanded }
                            }
                        }
                    }
                } else {
                    items(state.items, key = { it.loggedItemId }) { item -> ExerciseCard(item) }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(state: SessionDetailUiState, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        HeaderIconButton(
            icon = MindSetIcons.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
        )
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            val meta = listOfNotNull(
                typeLabel(state.type).uppercase(),
                relativeDate(state.startedAt),
                state.totalTimeMs?.let { formatClockMs(it) },
            ).joinToString("  ·  ")
            Text(meta, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            Text(
                state.name.ifEmpty { "Session" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun StatRow(state: SessionDetailUiState) {
    val unit = LocalWeightUnit.current
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        StatTile(value = state.totalTimeMs?.let { formatClockMs(it) } ?: "—", label = "Total Time")
        StatTile(value = "${formatVolume(state.totalVolumeKg, unit)} ${Units.label(unit)}", label = "Volume")
    }
}

@Composable
private fun RowScope.StatTile(value: String, label: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .weight(1f)
            .glassSurface()
            .padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Timeline ──────────────────────────────────────────────────────────────────────────────────

private val GutterWidth = 32.dp
private val DotSize = 16.dp
private val RailWidth = 2.dp
private val StationIconSize = 20.dp
private val RunIconSize = 16.dp

@Composable
private fun TimelineGutter(showDot: Boolean, isFirst: Boolean, isLast: Boolean) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.width(GutterWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
        val line = when {
            isFirst && isLast -> Modifier.fillMaxHeight()
            isFirst -> Modifier
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter) // from the top dot, downward
            isLast -> Modifier
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter) // ends at the last node
            else -> Modifier.fillMaxHeight()
        }
        Box(line.width(RailWidth).background(colors.outlineVariant))
        if (showDot) Box(Modifier.size(DotSize).clip(CircleShape).background(colors.primary))
    }
}

@Composable
private fun TimelineRow(
    showDot: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineGutter(showDot = showDot, isFirst = isFirst, isLast = isLast)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Box(Modifier.weight(1f).padding(bottom = MaterialTheme.spacing.smd)) { content() }
    }
}

@Composable
private fun StationBody(item: LoggedItemUi, stationNumber: Int) {
    val colors = MaterialTheme.colorScheme
    val set = item.sets.firstOrNull()
    val context = set?.let(::segmentContext)
    Row(
        modifier = Modifier.fillMaxWidth().glassSurface()
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(
            UIHelper.stationIcon(item.segmentKey),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(StationIconSize),
        )
        Column(Modifier.weight(1f)) {
            Text(
                "STATION $stationNumber",
                style = MaterialTheme.typography.labelSmall,
                color = colors.outline,
            )
            Text(
                item.exerciseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (context != null) {
                Text(
                    context,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Text(
                splitText(set),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun RunBody(item: LoggedItemUi) {
    val colors = MaterialTheme.colorScheme
    val set = item.sets.firstOrNull()
    val dist = set?.let { it.distanceM ?: it.targetDistanceM }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .padding(
                horizontal = MaterialTheme.spacing.md,
                vertical = MaterialTheme.spacing.smd,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(
            MindSetIcons.Run,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(RunIconSize),
        )
        Text(
            dist?.let { "${formatDistance(it)} ${item.exerciseName}" } ?: item.exerciseName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            splitText(set),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun RemainingToggle(expanded: Boolean, remaining: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val rotation by animateFloatAsState(if (expanded) 270f else 90f, label = "chevron")
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineGutter(showDot = false, isFirst = false, isLast = true)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Row(
            modifier = Modifier.weight(1f).clip(shape).border(1.dp, GlassBorder, shape)
                .clickable(onClick = onClick).padding(vertical = MaterialTheme.spacing.smd),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (expanded) "Show Less"
                else "View $remaining Remaining Stations",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Icon(
                MindSetIcons.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(RunIconSize).rotate(rotation),
            )
        }
    }
}

@Composable
private fun ExerciseCard(item: LoggedItemUi) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    Column(
        modifier = Modifier.fillMaxWidth().glassSurface().padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(
            item.exerciseName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${item.sets.size} sets",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        item.sets.forEach { set ->
            Text(
                set.detailSummary(item.captureFields, unit),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
        }
    }
}

private fun splitText(set: SetEntry?): String = set?.timeSec?.let { formatClockMs(it * 1000L) } ?: "—"

private fun segmentContext(set: SetEntry): String? {
    (set.distanceM ?: set.targetDistanceM)?.let { return "$it m" }
    (set.reps ?: set.targetReps)?.let { return "$it reps" }
    return null
}

private fun formatDistance(m: Int): String = if (m % 1000 == 0 && m >= 1000) "${m / 1000}km" else "${m}m"
