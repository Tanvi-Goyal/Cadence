package dev.cadence

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.data.local.Session
import dev.cadence.presentation.HomeUiState
import dev.cadence.presentation.HomeViewModel
import dev.cadence.presentation.SyncStatusUi
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Background = Color(0xFF0E0E0F)
private val Surface = Color(0xFF1A1A1C)
private val Accent = Color(0xFFC7F04D)
private val OnAccent = Color(0xFF14210A)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF8E8E93)

private val CadenceColors = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MaterialTheme(colorScheme = CadenceColors) {
        Scaffold(
            containerColor = Background,
            bottomBar = { CadenceBottomBar() },
        ) { padding ->
            HomeContent(
                state = state,
                onNewSession = viewModel::onNewSessionClick,
                onSync = viewModel::onSyncClick,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onNewSession: () -> Unit,
    onSync: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        HomeHeader(syncStatus = state.syncStatus, onSync = onSync)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNewSession,
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("+  New session", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
        Text("Recent", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        if (state.sessions.isEmpty()) {
            Text(
                "No sessions yet. Start your first one.",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.sessions, key = { it.id }) { session ->
                    SessionRow(session)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(syncStatus: SyncStatusUi, onSync: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("TODAY", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
}

/** Subtle sync affordance (PRD's "last synced" indicator): tap to sync, reflects current state. */
@Composable
private fun SyncIndicator(syncStatus: SyncStatusUi, onSync: () -> Unit) {
    val label = when (syncStatus) {
        SyncStatusUi.Idle -> "Sync"
        SyncStatusUi.Syncing -> "Syncing…"
        SyncStatusUi.Error -> "Retry"
    }
    val color = if (syncStatus == SyncStatusUi.Error) Color(0xFFE24B4A) else TextSecondary
    Text(
        text = label,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(enabled = syncStatus != SyncStatusUi.Syncing, onClick = onSync)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun SessionRow(session: Session) {
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
                .background(Background),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Session", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(formatTimestamp(session.startedAt), color = TextSecondary, fontSize = 13.sp)
        }
    }
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
                Text(
                    label,
                    color = if (active) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat(
        "EEE, d MMM · HH:mm",
        Locale.getDefault()
    ).format(
        Date(epochMillis)
    )

@Preview
@Composable
private fun HomeContentPreview() {
    MaterialTheme(colorScheme = CadenceColors) {
        HomeContent(
            state = HomeUiState(),
            onNewSession = {},
            onSync = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}
