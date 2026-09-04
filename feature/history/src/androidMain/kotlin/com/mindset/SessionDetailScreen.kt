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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.domain.LoggedItemUi
import com.mindset.domain.Units
import com.mindset.domain.detailSummary
import com.mindset.icons.ArrowBack
import com.mindset.icons.Bolt
import com.mindset.icons.Burpee
import com.mindset.icons.ChevronRight
import com.mindset.icons.Dumbbell
import com.mindset.icons.LowerBody
import com.mindset.icons.Rowing
import com.mindset.icons.Run
import com.mindset.icons.SkiErg
import com.mindset.icons.SledPull
import com.mindset.icons.Timer
import com.mindset.icons.WallBall
import com.mindset.model.SetEntry
import com.mindset.presentation.SessionDetailUiState
import com.mindset.presentation.SessionDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Stations shown before the "View N Remaining" collapse. A full Hyrox has 8. */
private const val STATION_LIMIT = 4

@Composable
fun SessionDetailScreen(sessionId: String, onBack: () -> Unit, viewModel: SessionDetailViewModel = koinViewModel { parametersOf(sessionId) }) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        val colors = MaterialTheme.colorScheme

        // Order-preserving station numbering: runs (segmentKey contains "run") and non-segment items
        // get no number; other segments count up 1..8. Same rule as Log Workout.
        val numbered = remember(state.items) {
            var n = 0
            state.items.map { item ->
                val isStation = item.segmentKey?.let { "run" !in it } == true
                item to (if (isStation) ++n else null)
            }
        }
        val hasTimeline = state.items.any { it.segmentKey != null }
        val totalStations = numbered.count { it.second != null }
        // Index just past the STATION_LIMIT-th station (includes any runs before it).
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

        Scaffold(containerColor = colors.background) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.md,
                    end = MaterialTheme.spacing.md,
                    top = padding.calculateTopPadding() + MaterialTheme.spacing.smd,
                    bottom = padding.calculateBottomPadding() + MaterialTheme.spacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            ) {
                item(key = "header") { DetailHeader(state, onBack) }

                item(key = "stats") { StatRow(state) }

                item(key = "sectionLabel") {
                    if (hasTimeline) {
                        SectionHeader("Race Timeline", icon = MindSetIcons.Timer)
                    } else {
                        SectionHeader("Exercises")
                    }
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
                    // One item so the connecting line is continuous (no LazyColumn gaps between rows).
                    // A session has ≤ ~18 segments, so a single Column costs nothing.
                    item(key = "timeline") {
                        val visible = if (expanded) numbered else numbered.take(cutoff)
                        val hasToggle = remaining > 0
                        Column {
                            visible.forEachIndexed { i, (item, station) ->
                                TimelineRow(
                                    // Dot on every station, plus the first node so the line starts at the race start.
                                    showDot = station != null || i == 0,
                                    isFirst = i == 0,
                                    isLast = !hasToggle && i == visible.lastIndex,
                                ) {
                                    if (station != null) StationBody(item, station) else RunBody(item)
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

// ── Header ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailHeader(state: SessionDetailUiState, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd)) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceContainerHigh)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                MindSetIcons.ArrowBack,
                contentDescription = "Back",
                tint = colors.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            val meta = listOfNotNull(
                typeLabel(state.type).uppercase(),
                relativeDate(state.startedAt),
                state.totalTimeMs?.let { formatClock(it) },
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

// ── Stat tiles ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatRow(state: SessionDetailUiState) {
    val unit = LocalWeightUnit.current
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        StatTile(value = state.totalTimeMs?.let { formatClock(it) } ?: "—", label = "Total Time")
        StatTile(value = "${formatVolume(state.totalVolumeKg, unit)} ${Units.label(unit)}", label = "Volume")
    }
}

@Composable
private fun RowScope.StatTile(value: String, label: String) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier.weight(1f).clip(shape).background(GlassFill).border(1.dp, GlassBorder, shape)
            .padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
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

@Composable
private fun SectionHeader(text: String, icon: ImageVector? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        }
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.outline)
    }
}

// ── Timeline ──────────────────────────────────────────────────────────────────────────────────

private val GUTTER_WIDTH = 32.dp
private val DOT_SIZE = 16.dp

/** Leading rail: a vertical line (trimmed to start at the first node and end at the last) + optional dot. */
@Composable
private fun TimelineGutter(showDot: Boolean, isFirst: Boolean, isLast: Boolean) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.width(GUTTER_WIDTH).fillMaxHeight(), contentAlignment = Alignment.Center) {
        val line = when {
            isFirst && isLast -> Modifier.fillMaxHeight()
            isFirst -> Modifier.fillMaxHeight(0.5f).align(Alignment.BottomCenter) // from the top dot, downward
            isLast -> Modifier.fillMaxHeight(0.5f).align(Alignment.TopCenter) // ends at the last node
            else -> Modifier.fillMaxHeight()
        }
        Box(line.width(2.dp).background(colors.outlineVariant))
        if (showDot) Box(Modifier.size(DOT_SIZE).clip(CircleShape).background(colors.primary))
    }
}

/** One timeline row: the leading rail beside [content]. */
@Composable
private fun TimelineRow(showDot: Boolean, isFirst: Boolean, isLast: Boolean, content: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineGutter(showDot = showDot, isFirst = isFirst, isLast = isLast)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        // Bottom padding is the inter-row gap; the gutter fills through it so the line stays unbroken.
        Box(Modifier.weight(1f).padding(bottom = MaterialTheme.spacing.smd)) { content() }
    }
}

/** Compact station card: name on the left, its distance/reps + split stacked on the right. */
@Composable
private fun StationBody(item: LoggedItemUi, stationNumber: Int) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val set = item.sets.firstOrNull()
    val context = set?.let(::segmentContext)
    Row(
        modifier = Modifier.fillMaxWidth().clip(shape).background(GlassFill).border(1.dp, GlassBorder, shape)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(stationIcon(item.segmentKey), contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text("STATION $stationNumber", style = MaterialTheme.typography.labelSmall, color = colors.outline)
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
                Text(context, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Text(splitText(set), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
        }
    }
}

/** Run row — a secondary (filled, borderless) card, lighter than a station. */
@Composable
private fun RunBody(item: LoggedItemUi) {
    val colors = MaterialTheme.colorScheme
    val set = item.sets.firstOrNull()
    val dist = set?.let { it.distanceM ?: it.targetDistanceM }
    Row(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.surfaceContainerLow)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(MindSetIcons.Run, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
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
                if (expanded) "Show Less" else "View $remaining Remaining Stations",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Icon(
                MindSetIcons.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp).rotate(rotation),
            )
        }
    }
}

// ── Non-Hyrox fallback card ─────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCard(item: LoggedItemUi) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).background(GlassFill).border(1.dp, GlassBorder, shape)
            .padding(MaterialTheme.spacing.md),
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
        Text("${item.sets.size} sets", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        item.sets.forEach { set ->
            Text(
                set.detailSummary(item.captureFields, unit),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────────────────────

/** Logged split (actual time) as m:ss, or an em-dash when nothing was recorded. */
private fun splitText(set: SetEntry?): String = set?.timeSec?.let { formatClock(it * 1000L) } ?: "—"

/** Distance/reps context beside a station name (e.g. "1000 m" or "100 reps"); null if neither set. */
private fun segmentContext(set: SetEntry): String? {
    (set.distanceM ?: set.targetDistanceM)?.let { return "$it m" }
    (set.reps ?: set.targetReps)?.let { return "$it reps" }
    return null
}

private fun formatDistance(m: Int): String = if (m % 1000 == 0 && m >= 1000) "${m / 1000}km" else "${m}m"

/** Per-station glyph — mirrors Log Workout's mapping (no bespoke icon → nearest fallback). */
private fun stationIcon(segmentKey: String?): ImageVector = when {
    segmentKey == null -> MindSetIcons.Bolt
    "run" in segmentKey -> MindSetIcons.Run
    "ski" in segmentKey -> MindSetIcons.SkiErg
    "sled" in segmentKey -> MindSetIcons.SledPull
    "burpee" in segmentKey -> MindSetIcons.Burpee
    "rowing" in segmentKey -> MindSetIcons.Rowing
    "farmers" in segmentKey -> MindSetIcons.Dumbbell
    "sandbag" in segmentKey || "lunge" in segmentKey -> MindSetIcons.LowerBody
    "wall-ball" in segmentKey -> MindSetIcons.WallBall
    else -> MindSetIcons.Bolt
}
