package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.data.local.PlannedSession
import dev.cadence.data.local.Session
import dev.cadence.data.local.SessionType
import dev.cadence.presentation.HomeStats
import dev.cadence.presentation.HomeUiState
import dev.cadence.presentation.HomeViewModel
import dev.cadence.presentation.SyncStatusUi
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSession: (String) -> Unit,
    onNewSession: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Starting the planned session creates it, then emits its id here to open Log Workout.
    LaunchedEffect(Unit) {
        viewModel.openSession.collect { onOpenSession(it) }
    }
    CadenceTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = { CadenceBottomBar() },
        ) { padding ->
            HomeContent(
                state = state,
                onStartPlanned = viewModel::onStartPlannedSession,
                onNewSession = onNewSession,
                onSync = viewModel::onSyncClick,
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
    onSync: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.background(Background),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HomeHeader(
                syncStatus = state.syncStatus,
                syncError = state.syncError,
                onSync = onSync,
            )
        }
        state.plannedSession?.let { plan ->
            item {
                Spacer(Modifier.height(12.dp))
                TodayCard(plan = plan, onStart = onStartPlanned)
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "+ New session",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNewSession)
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        item {
            Spacer(Modifier.height(4.dp))
            StatsRow(stats = state.stats)
        }
        item {
            Spacer(Modifier.height(12.dp))
            SectionHeader()
        }
        if (state.sessions.isEmpty()) {
            item {
                Text(
                    "No sessions yet. Start your first one.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                )
            }
        } else {
            items(state.sessions, key = { it.id }) { session ->
                SessionRow(session, volumeKg = state.volumeBySession[session.id] ?: 0.0)
            }
        }
    }
}

@Composable
private fun HomeHeader(syncStatus: SyncStatusUi, syncError: String?, onSync: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    todayLabel(),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text("Hey, Tanvi", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SyncIndicator(syncStatus = syncStatus, onSync = onSync)
                Spacer(Modifier.size(12.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("T", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (syncStatus == SyncStatusUi.Error && syncError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Sync failed — $syncError",
                color = Danger,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Sync affordance (PRD's "last synced" indicator): tap to sync, reflects current state. */
@Composable
private fun SyncIndicator(syncStatus: SyncStatusUi, onSync: () -> Unit) {
    val label = when (syncStatus) {
        SyncStatusUi.Idle -> "Sync"
        SyncStatusUi.Syncing -> "Syncing…"
        SyncStatusUi.Error -> "Retry"
    }
    Text(
        text = label,
        color = if (syncStatus == SyncStatusUi.Error) Danger else TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(enabled = syncStatus != SyncStatusUi.Syncing, onClick = onSync)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/** The Figma "Today" card, backed by a real (seeded) planned session. */
@Composable
private fun TodayCard(plan: PlannedSession, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "TODAY",
                color = OnAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Text("~${plan.targetDurationMin} min", color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(plan.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(plan.focus, color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Start session", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatsRow(stats: HomeStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(value = stats.total.toString(), label = "Sessions", modifier = Modifier.weight(1f))
        StatTile(value = formatVolume(stats.totalVolumeKg), label = "Volume kg", modifier = Modifier.weight(1f))
        StatTile(value = stats.dayStreak.toString(), label = "Day streak", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(vertical = 16.dp, horizontal = 12.dp),
    ) {
        Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun SectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Recent", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Text("See all", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SessionRow(session: Session, volumeKg: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceHi),
            contentAlignment = Alignment.Center,
        ) {
            Text(typeBadge(session.type), color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(session.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(relativeDate(session.startedAt), color = TextSecondary, fontSize = 13.sp)
        }
        if (volumeKg > 0.0) {
            Text(
                "${formatVolume(volumeKg)} kg",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Compact volume: 12,400 → "12.4k", 850 → "850". */
private fun formatVolume(kg: Double): String {
    val v = kg.toInt()
    return if (v >= 1000) "${(v / 100) / 10.0}k" else v.toString()
}

@Composable
private fun CadenceBottomBar() {
    val items = listOf("Home", "History", "Stats", "Profile")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEachIndexed { index, label ->
            val active = index == 0
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) Accent else Surface),
                )
                Spacer(Modifier.height(6.dp))
                Text(label, color = if (active) TextPrimary else TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

private fun typeBadge(type: String): String = when (type) {
    SessionType.CONDITIONING -> "C"
    SessionType.HYROX -> "H"
    SessionType.MIXED -> "M"
    else -> "S"
}

/** Header label like "MONDAY · 12 JUL", from the device clock. */
private fun todayLabel(): String =
    SimpleDateFormat("EEEE · d MMM", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())

/** "Today" / "Yesterday" / "Sun, 12 Jul" — day-relative, computed in the Android UI layer. */
private fun relativeDate(epochMillis: Long): String {
    val cal = Calendar.getInstance()
    val startOfToday = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val dayMs = 86_400_000L
    return when (((startOfToday - epochMillis) / dayMs)) {
        in Long.MIN_VALUE..-1L, 0L -> "Today"
        1L -> "Yesterday"
        else -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(epochMillis))
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    CadenceTheme {
        HomeContent(
            state = HomeUiState(
                plannedSession = PlannedSession("1", "Upper Strength", SessionType.STRENGTH, 55, "Push focus"),
                stats = HomeStats(total = 4, dayStreak = 9, totalVolumeKg = 12400.0),
            ),
            onStartPlanned = {},
            onNewSession = {},
            onSync = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}
