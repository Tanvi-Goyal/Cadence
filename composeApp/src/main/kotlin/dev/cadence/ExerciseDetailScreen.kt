package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.cadence.data.local.Exercise
import dev.cadence.presentation.ExerciseDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onAdd: () -> Unit,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = koinViewModel { parametersOf(exerciseId) },
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
    CadenceTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                ) {
                    Text("Add to workout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            },
        ) { padding ->
            val ex = exercise
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        Text(ex?.name ?: "Exercise", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (ex != null) {
                    ex.imageUrlsList.firstOrNull()?.let { url ->
                        item {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
                                    .clip(RoundedCornerShape(16.dp)).background(Surface),
                            )
                        }
                    }
                    item { TagRow(ex) }
                    item { MuscleChips(ex) }
                    if (ex.instructionsList.isNotEmpty()) {
                        item { Text("Instructions", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                        itemsIndexedSteps(ex.instructionsList)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexedSteps(steps: List<String>) {
    items(steps.size) { i ->
        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Text("${i + 1}. ", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(steps[i], color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TagRow(ex: Exercise) {
    val tags = listOfNotNull(ex.level, ex.equipment, ex.mechanic, ex.force)
        .map { it.replaceFirstChar { c -> c.uppercase() } }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tags) { tag -> Pill(tag, filled = false) }
    }
}

@Composable
private fun MuscleChips(ex: Exercise) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ex.primaryMusclesList) { m -> Pill(m.replaceFirstChar { it.uppercase() }, filled = true) }
        items(ex.secondaryMusclesList) { m -> Pill(m.replaceFirstChar { it.uppercase() }, filled = false) }
    }
}

@Composable
private fun Pill(label: String, filled: Boolean) {
    Box(
        modifier = Modifier.clip(CircleShape).background(if (filled) Accent else SurfaceHi)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(label, color = if (filled) OnAccent else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
