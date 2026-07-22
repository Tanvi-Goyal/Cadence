package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.data.local.ExerciseMetric
import dev.cadence.domain.Units
import dev.cadence.domain.WeightUnit
import dev.cadence.model.SetEntry
import dev.cadence.presentation.LoggedItemUi
import dev.cadence.presentation.SessionDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CadenceTheme {
        Scaffold(containerColor = Background) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "‹",
                            color = TextPrimary,
                            fontSize = 28.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                        )
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(state.name.ifEmpty { "Session" }, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            val unit = LocalWeightUnit.current
                            Text(
                                "${relativeDate(state.startedAt)} · ${formatVolume(state.totalVolumeKg, unit)} ${Units.label(unit)}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
                if (state.items.isEmpty()) {
                    item { Text("No exercises logged.", color = TextSecondary, fontSize = 14.sp) }
                } else {
                    items(state.items, key = { it.loggedItemId }) { item -> DetailCard(item) }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(item: LoggedItemUi) {
    val isStrength = item.metric == ExerciseMetric.WEIGHT_REPS
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Text(item.exerciseName, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text("${item.sets.size} sets", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.size(8.dp))
        val unit = LocalWeightUnit.current
        item.sets.forEach { set -> Text(setLine(set, isStrength, unit), color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp)) }
    }
}

private fun setLine(set: SetEntry, isStrength: Boolean, unit: WeightUnit): String =
    if (isStrength) "Set ${set.setNumber}:  ${set.reps ?: 0} reps × ${formatVolume((set.loadKg ?: 0.0), unit)} ${Units.label(unit)}"
    else "Set ${set.setNumber}:  ${set.timeSec ?: 0}s · ${set.distanceM ?: 0} m"
