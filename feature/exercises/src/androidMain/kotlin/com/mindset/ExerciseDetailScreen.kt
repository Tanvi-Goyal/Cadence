package com.mindset

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
import androidx.compose.material3.MaterialTheme
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
import com.mindset.model.Exercise
import com.mindset.presentation.ExerciseDetailViewModel
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
    val muscleDiagram by viewModel.muscleDiagram.collectAsStateWithLifecycle()
    MindSetTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                ) {
                    Text("Add to workout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            },
        ) { padding ->
            val ex = exercise
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 28.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(ex?.name ?: "Exercise", color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (ex != null) {
                    ex.imageUrls.firstOrNull()?.let { url ->
                        item {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
                                    .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer),
                            )
                        }
                    }
                    item { TagRow(ex) }
                    item { MuscleChips(ex) }
                    muscleDiagram?.let { diagram ->
                        item {
                            Column {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(220.dp)
                                        .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    // Base body silhouette + highlighted-muscle overlay, layered
                                    // (same wger canvas, so identical bounds align them).
                                    AsyncImage(model = diagram.baseUrl, contentDescription = null,
                                        modifier = Modifier.fillMaxSize())
                                    AsyncImage(model = diagram.overlayUrl, contentDescription = "Target muscle",
                                        modifier = Modifier.fillMaxSize())
                                }
                                Text(
                                    "Muscle diagram: wger.de · CC-BY-SA",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                    if (ex.instructions.isNotEmpty()) {
                        item { Text("Instructions", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                        itemsIndexedSteps(ex.instructions)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexedSteps(steps: List<String>) {
    items(steps.size) { i ->
        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Text("${i + 1}. ", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(steps[i], color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
        items(ex.primaryMuscles) { m -> Pill(m.replaceFirstChar { it.uppercase() }, filled = true) }
        items(ex.secondaryMuscles) { m -> Pill(m.replaceFirstChar { it.uppercase() }, filled = false) }
    }
}

@Composable
private fun Pill(label: String, filled: Boolean) {
    Box(
        modifier = Modifier.clip(CircleShape).background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(label, color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
