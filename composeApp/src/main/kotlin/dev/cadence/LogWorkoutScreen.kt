package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.data.local.ExerciseMetric
import dev.cadence.domain.Units
import dev.cadence.domain.WeightUnit
import dev.cadence.data.local.SetEntry
import dev.cadence.presentation.LoggedItemUi
import dev.cadence.presentation.LogWorkoutViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LogWorkoutScreen(
    sessionId: String,
    pickedExerciseId: String?,
    onExerciseConsumed: () -> Unit,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onFinish: () -> Unit,
    viewModel: LogWorkoutViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // An exercise picked in the ExercisePicker is added here — in THIS screen's (alive) VM scope,
    // so the write can't be cancelled by the picker being popped off the back stack.
    LaunchedEffect(pickedExerciseId) {
        if (pickedExerciseId != null) {
            viewModel.addExercise(pickedExerciseId)
            onExerciseConsumed()
        }
    }
    CadenceTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                ) {
                    Text("Finish session", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
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
                        Text(
                            state.sessionName.ifEmpty { "Session" },
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                items(state.items, key = { it.loggedItemId }) { item ->
                    ExerciseCard(
                        item = item,
                        onAddStrengthSet = { reps, kg -> viewModel.addStrengthSet(item.loggedItemId, reps, kg) },
                        onAddCardioSet = { sec, m -> viewModel.addCardioSet(item.loggedItemId, sec, m) },
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
private fun ExerciseCard(
    item: LoggedItemUi,
    onAddStrengthSet: (Int, Double) -> Unit,
    onAddCardioSet: (Int, Int) -> Unit,
) {
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
        Spacer(Modifier.height(12.dp))
        item.sets.forEach { set -> SetRow(set, isStrength) }
        Spacer(Modifier.height(8.dp))
        AddSetRow(isStrength = isStrength, onAddStrengthSet = onAddStrengthSet, onAddCardioSet = onAddCardioSet)
    }
}

@Composable
private fun SetRow(set: SetEntry, isStrength: Boolean) {
    val unit = LocalWeightUnit.current
    val text = if (isStrength) {
        "Set ${set.setNumber}:  ${set.reps ?: 0} reps × ${formatKg(set.loadKg, unit)} ${Units.label(unit)}"
    } else {
        "Set ${set.setNumber}:  ${set.timeSec ?: 0}s · ${set.distanceM ?: 0} m"
    }
    Text(text, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun AddSetRow(
    isStrength: Boolean,
    onAddStrengthSet: (Int, Double) -> Unit,
    onAddCardioSet: (Int, Int) -> Unit,
) {
    val unit = LocalWeightUnit.current
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField(
            value = first,
            onValueChange = { first = it },
            label = if (isStrength) "reps" else "sec",
            modifier = Modifier.weight(1f),
        )
        NumberField(
            value = second,
            onValueChange = { second = it },
            label = if (isStrength) Units.label(unit) else "m",
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
                            // Input is in the display unit; store canonical kg.
                            onAddStrengthSet(reps, Units.toKg(entered, unit)); first = ""; second = ""
                        }
                    } else {
                        val sec = first.toIntOrNull()
                        val m = second.toIntOrNull()
                        if (sec != null && m != null) {
                            onAddCardioSet(sec, m); first = ""; second = ""
                        }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Add", color = OnAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = TextSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceHi,
            unfocusedContainerColor = SurfaceHi,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}

private fun formatKg(kg: Double?, unit: WeightUnit): String {
    if (kg == null) return "0"
    val v = Units.toDisplay(kg, unit)
    return if (v % 1.0 == 0.0) v.toInt().toString() else ((v * 10).toInt() / 10.0).toString()
}
