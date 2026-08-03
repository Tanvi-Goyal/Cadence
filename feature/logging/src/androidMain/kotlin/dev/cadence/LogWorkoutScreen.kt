package dev.cadence

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.domain.LoggedItemUi
import dev.cadence.domain.LogSectionUi
import dev.cadence.domain.Units
import dev.cadence.domain.WeightUnit
import dev.cadence.icons.Add
import dev.cadence.icons.ArrowBack
import dev.cadence.icons.Dumbbell
import dev.cadence.icons.Play
import dev.cadence.model.CaptureFields
import dev.cadence.model.MetricType
import dev.cadence.model.SetEntry
import dev.cadence.presentation.LogWorkoutViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/*
 * Log Workout — the live tracking screen for one session (Kinetic design). Reads the DB-hydrated
 * session as ordered sections (warm-up / main / … / core), and renders each exercise's sets as
 * metric-aware rows (driven by CaptureFields). A set instantiated from a template shows its
 * prescription as a GHOST (prefilled, faint target); tapping ✓ writes the actual. Rest-timer / pause
 * / ongoing-notification are the separate tracker slice.
 */

private val Gutter = 20.dp

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
    // An exercise picked in the ExercisePicker is added here (this screen's alive VM scope) so the
    // write can't be cancelled by the picker being popped off the back stack.
    LaunchedEffect(pickedExerciseId) {
        if (pickedExerciseId != null) {
            viewModel.addExercise(pickedExerciseId)
            onExerciseConsumed()
        }
    }
    CadenceTheme {
        val colors = MaterialTheme.colorScheme
        Scaffold(containerColor = colors.background) { inner ->
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = inner.calculateTopPadding(),
                        bottom = inner.calculateBottomPadding() + 108.dp,
                        start = Gutter,
                        end = Gutter,
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item(key = "header") { TopBar(state.sessionName.ifEmpty { "Session" }, onBack) }
                    state.sections.forEach { section ->
                        item(key = "sec-${section.label}") { SectionHeader(section.label, section.meta) }
                        items(section.items, key = { it.loggedItemId }) { item ->
                            ExerciseCard(
                                item = item,
                                onUpdate = viewModel::updateActual,
                                onAddSet = { r, l, t, d -> viewModel.addSet(item.loggedItemId, r, l, t, d) },
                            )
                        }
                    }
                    item(key = "add-exercise") { AddExerciseButton(onAddExercise) }
                }

                FinishCta(onFinish, Modifier.align(Alignment.BottomStart).padding(bottom = inner.calculateBottomPadding()))
            }
        }
    }
}

// ── Top bar / section header ──────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Icon(CadenceIcons.ArrowBack, contentDescription = "Back", tint = colors.onSurface, modifier = Modifier.size(16.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.onSurface)
    }
}

@Composable
private fun SectionHeader(label: String, meta: String?) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = colors.onSurfaceVariant)
        if (meta != null) {
            Box(Modifier.clip(CircleShape).background(colors.primary.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
        }
    }
}

// ── Exercise card ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCard(
    item: LoggedItemUi,
    onUpdate: (SetEntry) -> Unit,
    onAddSet: (Int?, Double?, Int?, Int?) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    val capture = remember(item.metric) { CaptureFields.of(MetricType.valueOf(item.metric)) }
    Column(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.surfaceContainer).border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: medallion + name (+ ES) + note.
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(colors.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                Icon(CadenceIcons.Dumbbell, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.exerciseName, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                    if (item.eachSide) EsBadge()
                }
                if (!item.note.isNullOrBlank()) {
                    Text(item.note!!, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        // Set rows.
        item.sets.forEach { set ->
            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.15f))
            SetRow(capture, set, unit, onUpdate)
        }
        AddSetRow(capture, unit, onAddSet)
    }
}

@Composable
private fun EsBadge() {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.clip(CircleShape).background(colors.surfaceContainerHighest).padding(horizontal = 6.dp, vertical = 1.dp)) {
        Text("ES", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

// ── Set rows (metric-aware, ghost target → actual) ────────────────────────────────────────────

@Composable
private fun SetRow(capture: CaptureFields, set: SetEntry, unit: WeightUnit, onUpdate: (SetEntry) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val logged = isLogged(capture, set)

    // Prefill from the actual if logged, else the ghost target. Keyed to the set id so each row owns
    // stable input state and one row's edits don't disturb its siblings.
    val (prefillA, prefillB) = remember(set.id) { prefills(capture, set, unit) }
    var a by remember(set.id) { mutableStateOf(prefillA) }
    var b by remember(set.id) { mutableStateOf(prefillB) }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.width(44.dp))
        val fields = captureFieldLabels(capture, unit)
        NumberField(value = a, onValueChange = { a = it }, placeholder = fields.first, modifier = Modifier.weight(1f))
        if (fields.second != null) {
            NumberField(value = b, onValueChange = { b = it }, placeholder = fields.second!!, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        // ✓ — filled primary when this set already holds a logged actual.
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (logged) colors.primary else colors.surfaceContainerHigh)
                .clickable { buildActual(capture, set, a, b, unit)?.let(onUpdate) },
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", color = if (logged) colors.onPrimary else colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddSetRow(capture: CaptureFields, unit: WeightUnit, onAddSet: (Int?, Double?, Int?, Int?) -> Unit) {
    if (capture is CaptureFields.Calories) return // addSet has no calories cell
    val colors = MaterialTheme.colorScheme
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    val fields = captureFieldLabels(capture, unit)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(Modifier.width(44.dp))
        NumberField(value = a, onValueChange = { a = it }, placeholder = fields.first, modifier = Modifier.weight(1f))
        if (fields.second != null) {
            NumberField(value = b, onValueChange = { b = it }, placeholder = fields.second!!, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        Box(
            modifier = Modifier.size(44.dp).clip(MaterialTheme.shapes.small).background(colors.surfaceContainerHigh).clickable {
                addFromInputs(capture, a, b, unit, onAddSet)
                a = ""; b = ""
            },
            contentAlignment = Alignment.Center,
        ) {
            Icon(CadenceIcons.Add, contentDescription = "Add set", tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AddExerciseButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium).clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(CadenceIcons.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Add Exercise", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = colors.primary)
    }
}

// ── Finish CTA ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun FinishCta(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxWidth().background(Brush.verticalGradient(0f to Color.Transparent, 0.5f to colors.background, 1f to colors.background)).padding(top = 32.dp, bottom = 20.dp, start = Gutter, end = Gutter),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(16.dp, CircleShape).clip(CircleShape).background(colors.primary).clickable(onClick = onFinish),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(CadenceIcons.Play, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(10.dp))
            Text("Finish Session".uppercase(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = colors.onPrimary)
        }
    }
}

// ── Metric helpers ────────────────────────────────────────────────────────────────────────────

/** Whether a set already holds a logged actual (vs. a target-only ghost). */
private fun isLogged(capture: CaptureFields, s: SetEntry): Boolean = when (capture) {
    CaptureFields.WeightReps -> s.reps != null || s.loadKg != null
    CaptureFields.RepsOnly -> s.reps != null
    CaptureFields.Duration -> s.timeSec != null
    CaptureFields.DistanceTime -> s.timeSec != null || s.distanceM != null
    CaptureFields.Calories -> s.calories != null
}

/** Placeholder labels for the (up to two) input cells of a capture type. */
private fun captureFieldLabels(capture: CaptureFields, unit: WeightUnit): Pair<String, String?> = when (capture) {
    CaptureFields.WeightReps -> "reps" to Units.label(unit)
    CaptureFields.RepsOnly -> "reps" to null
    CaptureFields.Duration -> "sec" to null
    CaptureFields.DistanceTime -> "sec" to "m"
    CaptureFields.Calories -> "cal" to null
}

/** Prefill the input cells from the actual if present, else the ghost target (display units). */
private fun prefills(capture: CaptureFields, s: SetEntry, unit: WeightUnit): Pair<String, String> = when (capture) {
    CaptureFields.WeightReps -> (s.reps ?: s.targetReps)?.toString().orEmpty() to kgPlain(s.loadKg ?: s.targetLoadKg, unit)
    CaptureFields.RepsOnly -> (s.reps ?: s.targetReps)?.toString().orEmpty() to ""
    CaptureFields.Duration -> (s.timeSec ?: s.targetTimeSec)?.toString().orEmpty() to ""
    CaptureFields.DistanceTime -> (s.timeSec ?: s.targetTimeSec)?.toString().orEmpty() to (s.distanceM ?: s.targetDistanceM)?.toString().orEmpty()
    CaptureFields.Calories -> (s.calories ?: s.targetCalories)?.toString().orEmpty() to ""
}

/** Build the edited set from the input strings, or null if the required cell is empty/invalid. */
private fun buildActual(capture: CaptureFields, s: SetEntry, a: String, b: String, unit: WeightUnit): SetEntry? = when (capture) {
    CaptureFields.WeightReps -> a.toIntOrNull()?.let { reps -> s.copy(reps = reps, loadKg = b.toDoubleOrNull()?.let { Units.toKg(it, unit) }) }
    CaptureFields.RepsOnly -> a.toIntOrNull()?.let { s.copy(reps = it) }
    CaptureFields.Duration -> a.toIntOrNull()?.let { s.copy(timeSec = it) }
    CaptureFields.DistanceTime -> a.toIntOrNull()?.let { sec -> s.copy(timeSec = sec, distanceM = b.toIntOrNull()) }
    CaptureFields.Calories -> a.toIntOrNull()?.let { s.copy(calories = it) }
}

private fun addFromInputs(capture: CaptureFields, a: String, b: String, unit: WeightUnit, onAddSet: (Int?, Double?, Int?, Int?) -> Unit) {
    when (capture) {
        CaptureFields.WeightReps -> a.toIntOrNull()?.let { onAddSet(it, b.toDoubleOrNull()?.let { kg -> Units.toKg(kg, unit) }, null, null) }
        CaptureFields.RepsOnly -> a.toIntOrNull()?.let { onAddSet(it, null, null, null) }
        CaptureFields.Duration -> a.toIntOrNull()?.let { onAddSet(null, null, it, null) }
        CaptureFields.DistanceTime -> a.toIntOrNull()?.let { onAddSet(null, null, it, b.toIntOrNull()) }
        CaptureFields.Calories -> Unit
    }
}

private fun kgPlain(kg: Double?, unit: WeightUnit): String {
    if (kg == null) return ""
    val v = Units.toDisplay(kg, unit)
    return if (v % 1.0 == 0.0) v.toInt().toString() else ((v * 10).toInt() / 10.0).toString()
}
