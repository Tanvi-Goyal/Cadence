package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.model.VolumePoint
import com.mindset.presentation.StatsUiState
import com.mindset.presentation.StatsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StatsScreen(
    onTab: (Tab) -> Unit,
    viewModel: StatsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { MindSetBottomBar(current = Tab.Stations, onTab = onTab) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Text("Trends", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Volume per session", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))

                if (state.exercises.isEmpty()) {
                    Text("Log some sets to see trends.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    return@Column
                }

                ExerciseSelector(state = state, onSelect = viewModel::onSelectExercise)
                Spacer(Modifier.height(20.dp))

                if (state.points.size < 2) {
                    Text(
                        "Log this exercise in at least 2 sessions to chart a trend.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                } else {
                    VolumeChart(points = state.points, modifier = Modifier.fillMaxWidth().height(220.dp))
                }
            }
        }
    }
}

@Composable
private fun ExerciseSelector(state: StatsUiState, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.exercises.forEach { ex ->
            val active = ex.id == state.selectedExerciseId
            Text(
                ex.name,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { onSelect(ex.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

/**
 * A hand-rolled bar chart (Compose [Canvas], no chart library — PRD §9). Each bar is one session's
 * volume for the selected exercise, oldest → newest; bar height scales to the max in the window.
 */
@Composable
private fun VolumeChart(points: List<VolumePoint>, modifier: Modifier = Modifier) {
    val max = points.maxOf { it.volume }.coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary // read the token in composable scope; the Canvas lambda is a DrawScope
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val n = points.size
            val gap = 12.dp.toPx()
            val barWidth = ((size.width - gap * (n - 1)) / n).coerceAtLeast(2f)
            val maxBarHeight = size.height
            points.forEachIndexed { i, point ->
                val h = (point.volume / max * maxBarHeight).toFloat()
                val left = i * (barWidth + gap)
                drawRect(
                    color = barColor,
                    topLeft = Offset(left, maxBarHeight - h),
                    size = Size(barWidth, h),
                )
            }
        }
    }
}
