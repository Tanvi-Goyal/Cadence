package dev.cadence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.model.PlannedSession
import dev.cadence.model.Session
import dev.cadence.ui.R
import dev.cadence.domain.Units
import dev.cadence.domain.WeightUnit
import dev.cadence.presentation.HomeStats
import dev.cadence.presentation.HomeUiState
import dev.cadence.presentation.HomeViewModel
import dev.cadence.presentation.SyncStatusUi
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

/** Screen-edge gutter for Home; the Figma frame uses a 20dp margin (not the 16dp grid default). */
private val ScreenGutter = 20.dp

@Composable
fun HomeScreen(
    onOpenSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    onTab: (Tab) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Starting the planned session creates it, then emits its id here to open Log Workout.
    LaunchedEffect(Unit) {
        viewModel.openSession.collect { onOpenSession(it) }
    }
    CadenceTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { CadenceBottomBar(current = Tab.Home, onTab = onTab) },
        ) { padding ->
            HomeContent(
                state = state,
                onStartPlanned = viewModel::onStartPlannedSession,
                onNewSession = onNewSession,
                onOpenTemplates = onOpenTemplates,
                onSync = viewModel::onSyncClick,
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
    onStartPlanned: () -> Unit,
    onNewSession: () -> Unit,
    onOpenTemplates: () -> Unit,
    onSync: () -> Unit,
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
            start = ScreenGutter,
            end = ScreenGutter,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item { HomeHeader(state.syncStatus, state.syncError, onSync, onNewSession) }

        item { SummaryMetrics(state.stats) }

        state.plannedSession?.let { plan ->
            item { UpNextSection(plan, onStartPlanned) }
        }

        item { TemplatesSection(onOpenTemplates) }

        item {
            RecentSection(
                sessions = state.sessions,
                volumeBySession = state.volumeBySession,
                onOpenDetail = onOpenDetail,
                onSeeAll = onSeeAll,
            )
        }

        item { WeeklyChallengeCard() }
    }
}

@Composable
private fun HomeHeader(
    syncStatus: SyncStatusUi,
    syncError: String?,
    onSync: () -> Unit,
    onNewSession: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                HeaderIconButton(
                    icon = CadenceIcons.Sync,
                    contentDescription = "Sync",
                    tint = if (syncStatus == SyncStatusUi.Error) colors.error else colors.primary,
                    enabled = syncStatus != SyncStatusUi.Syncing,
                    onClick = onSync,
                )
                HeaderIconButton(
                    icon = CadenceIcons.Add,
                    contentDescription = "New session",
                    tint = colors.primary,
                    onClick = onNewSession,
                )
            }
        }
        if (syncStatus == SyncStatusUi.Error && syncError != null) {
            Text(
                text = "Sync failed — $syncError",
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SummaryMetrics(stats: HomeStats) {
    val unit = LocalWeightUnit.current
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        StatTile(value = stats.total.toString(), label = "Sessions", modifier = Modifier.weight(1f))
        StatTile(value = stats.dayStreak.toString(), label = "Day streak", modifier = Modifier.weight(1f))
        StatTile(
            value = formatVolume(stats.totalVolumeKg, unit),
            label = "Volume (${Units.label(unit)})",
            modifier = Modifier.weight(1f),
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
                AccentPill(text = typeLabel(plan.type.name), icon = CadenceIcons.Bolt)
            }
        }
    }
}

@Composable
private fun TemplatesSection(onOpenTemplates: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        SectionLabel("Templates")
        // NOTE: HomeViewModel does not expose quick-start templates yet; this is a single
        // placeholder chip routing to the full Templates screen. Wiring real template chips here
        // needs a ViewModel/data change (out of scope for the theme + Home slice).
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            CadenceChip(
                label = "Browse templates",
                icon = CadenceIcons.Dumbbell,
                onClick = onOpenTemplates,
            )
        }
    }
}

@Composable
private fun RecentSection(
    sessions: List<Session>,
    volumeBySession: Map<String, Double>,
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
                        volumeKg = volumeBySession[session.id] ?: 0.0,
                        onClick = { onOpenDetail(session.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChallengeCard() {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(MaterialTheme.shapes.large),
    ) {
        Image(
            painter = painterResource(R.drawable.hero_weekly_challenge),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, colors.background),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                text = "Weekly Challenge",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
            Text(
                text = "Full Body Capacity",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}


@Preview
@Composable
private fun HomeContentPreview() {
    CadenceTheme {
        HomeContent(
            state = HomeUiState(
                plannedSession = PlannedSession("1", "Upper Strength", dev.cadence.model.SessionType.STRENGTH, 55, "Push focus"),
                stats = HomeStats(total = 20, dayStreak = 9, totalVolumeKg = 26700.0),
            ),
            onStartPlanned = {},
            onNewSession = {},
            onOpenTemplates = {},
            onSync = {},
            onOpenDetail = {},
            onSeeAll = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}
