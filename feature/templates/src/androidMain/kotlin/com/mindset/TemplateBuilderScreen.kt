package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.model.MetricType
import com.mindset.model.SetEntry
import com.mindset.domain.Units
import com.mindset.domain.WeightUnit
import com.mindset.domain.LoggedItemUi
import com.mindset.presentation.TemplateBuilderViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Builds a template: add exercises (via the shared picker) and **target** sets (the prescription).
 * Structurally mirrors [LogWorkoutScreen] but every set here is a plan, so it writes `target*` and
 * renders those values. Actuals are filled in later, once the template is started.
 */
@Composable
fun TemplateBuilderScreen(
    templateId: String,
    pickedExerciseId: String?,
    onExerciseConsumed: () -> Unit,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onDone: () -> Unit,
    viewModel: TemplateBuilderViewModel = koinViewModel { parametersOf(templateId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(pickedExerciseId) {
        if (pickedExerciseId != null) {
            viewModel.addExercise(pickedExerciseId)
            onExerciseConsumed()
        }
    }
    MindSetTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                ) {
                    Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 12.dp,
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
                            Text(
                                state.templateName.ifEmpty { "Template" },
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text("Prescription — targets to hit", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
                items(state.items, key = { it.loggedItemId }) { item ->
                    TargetExerciseCard(
                        item = item,
                        onAddStrengthTarget = { reps, kg -> viewModel.addTargetStrengthSet(item.loggedItemId, reps, kg) },
                        onAddCardioTarget = { sec, m -> viewModel.addTargetCardioSet(item.loggedItemId, sec, m) },
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, TextSecondary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onAddExercise)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+ Add exercise", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetExerciseCard(
    item: LoggedItemUi,
    onAddStrengthTarget: (Int, Double) -> Unit,
    onAddCardioTarget: (Int, Int) -> Unit,
) {
    val isStrength = item.metric == MetricType.WEIGHT_REPS.name
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Text(item.exerciseName, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text("${item.sets.size} target sets", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        item.sets.forEach { set -> TargetRow(set, isStrength) }
        Spacer(Modifier.height(8.dp))
        AddTargetRow(isStrength = isStrength, onAddStrengthTarget = onAddStrengthTarget, onAddCardioTarget = onAddCardioTarget)
    }
}

@Composable
private fun TargetRow(set: SetEntry, isStrength: Boolean) {
    val unit = LocalWeightUnit.current
    val text = if (isStrength) {
        "Set ${set.setNumber}:  ${set.targetReps ?: 0} reps × ${formatTargetKg(set.targetLoadKg, unit)} ${Units.label(unit)}"
    } else {
        "Set ${set.setNumber}:  ${set.targetTimeSec ?: 0}s · ${set.targetDistanceM ?: 0} m"
    }
    Text(text, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun AddTargetRow(
    isStrength: Boolean,
    onAddStrengthTarget: (Int, Double) -> Unit,
    onAddCardioTarget: (Int, Int) -> Unit,
) {
    val unit = LocalWeightUnit.current
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField(
            value = first,
            onValueChange = { first = it },
            placeholder = if (isStrength) "reps" else "sec",
            modifier = Modifier.weight(1f),
        )
        NumberField(
            value = second,
            onValueChange = { second = it },
            placeholder = if (isStrength) Units.label(unit) else "m",
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Accent)
                .clickable {
                    if (isStrength) {
                        val reps = first.toIntOrNull()
                        val entered = second.toDoubleOrNull()
                        if (reps != null && entered != null) {
                            onAddStrengthTarget(reps, Units.toKg(entered, unit)); first = ""; second = ""
                        }
                    } else {
                        val sec = first.toIntOrNull()
                        val m = second.toIntOrNull()
                        if (sec != null && m != null) {
                            onAddCardioTarget(sec, m); first = ""; second = ""
                        }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Add", color = OnAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatTargetKg(kg: Double?, unit: WeightUnit): String {
    if (kg == null) return "0"
    val v = Units.toDisplay(kg, unit)
    return if (v % 1.0 == 0.0) v.toInt().toString() else ((v * 10).toInt() / 10.0).toString()
}
