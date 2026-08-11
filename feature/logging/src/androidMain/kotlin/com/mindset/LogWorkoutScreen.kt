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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.GlassTextField
import com.mindset.components.PrimaryButton
import com.mindset.components.StepperField
import com.mindset.domain.LoggedItemUi
import com.mindset.domain.Units
import com.mindset.domain.WeightUnit
import com.mindset.icons.Add
import com.mindset.icons.Bolt
import com.mindset.icons.Burpee
import com.mindset.icons.Check
import com.mindset.icons.Close
import com.mindset.icons.Delete
import com.mindset.icons.Dumbbell
import com.mindset.icons.Info
import com.mindset.icons.LowerBody
import com.mindset.icons.Rowing
import com.mindset.icons.Run
import com.mindset.icons.SkiErg
import com.mindset.icons.SledPull
import com.mindset.icons.WallBall
import com.mindset.model.CaptureFields
import com.mindset.model.HyroxVariant
import com.mindset.model.SessionType
import com.mindset.model.SetEntry
import com.mindset.presentation.LogWorkoutViewModel
import com.mindset.presentation.StationOption
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutScreen(
    sessionId: String,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onFinish: () -> Unit,
    viewModel: LogWorkoutViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val standards by viewModel.stationStandards.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        val cards = remember(state.sections) { state.sections.flatMap { it.items } }

        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            bottomBar = { CompleteCta(onClick = { viewModel.finish(onFinish) }) },
        ) { inner ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + MaterialTheme.spacing.md,
                    bottom = inner.calculateBottomPadding() + MaterialTheme.spacing.md,
                    start = MaterialTheme.spacing.md,
                    end = MaterialTheme.spacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                item(key = "header") {
                    Header(
                        sessionType = state.sessionType,
                        startedAtMillis = state.startedAtMillis,
                        notes = state.notes,
                        sessionId = sessionId,
                        onBack = onBack,
                        onNotesChange = viewModel::onNotesChange,
                    )
                }

                item(key = "add") { AddCta(onClick = { showAddSheet = true }) }

                if (stations.isNotEmpty()) {
                    item(key = "quickAdd") { RaceQuickAdd(onPick = viewModel::addVariant) }
                }

                // Running station index for the "STATION n" tag — runs are not numbered.
                var stationNo = 0
                val numbered = cards.map { item ->
                    val key = item.segmentKey
                    val n = if (key != null && "run" !in key) ++stationNo else null
                    item to n
                }
                items(
                    numbered,
                    key = {
                        it.first.loggedItemId
                    },
                ) { (item, n) ->
                    if (item.segmentKey != null) {
                        StationCard(
                            item = item,
                            stationNumber = n,
                            standard = standards[item.segmentKey],
                            onUpdate = viewModel::updateActual,
                            onRemove = { viewModel.removeEntry(item.loggedItemId) },
                        )
                    }
                }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = colors.surfaceContainerLow,
            ) {
                AddToSessionSheet(
                    stations = stations,
                    onBrowseExercises = {
                        showAddSheet = false
                        onAddExercise()
                    },
                    onPickStation = { segmentKey ->
                        showAddSheet = false
                        viewModel.addStation(segmentKey)
                    },
                )
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun Header(
    sessionType: SessionType,
    startedAtMillis: Long,
    notes: String,
    sessionId: String,
    onBack: () -> Unit,
    onNotesChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var noteText by remember(sessionId) {
        mutableStateOf(notes)
    }

    LaunchedEffect(notes) {
        if (notes.isNotEmpty() && noteText.isEmpty()) noteText = notes
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = typeLabel(sessionType.name).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                )
                if (startedAtMillis > 0L) {
                    Text(
                        text = formatDateTime(startedAtMillis),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceContainerHigh)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    MindSetIcons.Close,
                    contentDescription = "Close",
                    tint = colors.onSurface,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Text(
            text = "Active Session",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )

        GlassTextField(
            value = noteText,
            onValueChange = {
                noteText = it
                onNotesChange(it)
            },
            placeholder = "Session notes (e.g. Focus on explosive push)",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

// // ── Entry card (strength / conditioning) ────────────────────────────────────────────────────────
//
// @Composable
// private fun EntryCard(
//    item: LoggedItemUi,
//    onUpdate: (SetEntry) -> Unit,
//    onAddSet: () -> Unit,
//    onRemove: () -> Unit,
// ) {
//    val colors = MaterialTheme.colorScheme
//    val unit = LocalWeightUnit.current
//    val capture = remember(item.metric) { CaptureFields.of(MetricType.valueOf(item.metric)) }
//    val isStrength = capture is CaptureFields.WeightReps || capture is CaptureFields.RepsOnly
//    val shape = MaterialTheme.shapes.medium
//
//    Column(
//        modifier = Modifier.fillMaxWidth().clip(shape).background(GlassFill)
//            .border(1.dp, GlassBorder, shape)
//            .padding(MaterialTheme.spacing.md),
//        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
//    ) {
//        // Header: medallion + name (+ES) + note; trailing muted tag.
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                Modifier.size(40.dp).clip(shape).background(colors.surfaceContainerHigh),
//                contentAlignment = Alignment.Center,
//            ) {
//                Icon(
//                    imageVector = if (isStrength) MindSetIcons.Dumbbell else MindSetIcons.Bolt,
//                    contentDescription = null,
//                    tint = colors.primary,
//                    modifier = Modifier.size(18.dp),
//                )
//            }
//            Column(
//                Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
//                ) {
//                    Text(
//                        item.exerciseName,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = colors.onSurface
//                    )
//                    if (item.eachSide) EsBadge()
//                }
//                if (!item.note.isNullOrBlank()) {
//                    Text(
//                        item.note!!,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = colors.onSurfaceVariant
//                    )
//                }
//            }
//            Text(
//                text = if (isStrength) "STRENGTH" else "CONDITIONING",
//                style = MaterialTheme.typography.labelSmall,
//                color = colors.outline,
//            )
//            DeleteButton(onRemove)
//        }
//
//        item.sets.forEach { set ->
//            SetRow(capture, set, showSetLabel = isStrength, unit = unit, onUpdate = onUpdate)
//        }
//
//        if (capture !is CaptureFields.Calories) {
//            AddSetButton(onAddSet)
//        }
//    }
// }

// ── Station card (Hyrox) ────────────────────────────────────────────────────────────────────────

@Composable
private fun StationCard(item: LoggedItemUi, stationNumber: Int?, standard: String?, onUpdate: (SetEntry) -> Unit, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    val shape = MaterialTheme.shapes.medium
    val set = item.sets.firstOrNull()

    // Fields the station captures: TIME always; + REPS (wall balls) or LOAD (loaded); + DIST if it has one.
    val second: SecondField = when {
        set?.targetReps != null -> SecondField.REPS
        set?.targetLoadKg != null -> SecondField.LOAD
        else -> SecondField.NONE
    }
    val hasDist = set?.targetDistanceM != null

    // Hoisted so the header ✓ commits what the fields row holds. TIME is empty-until-typed (dimmed
    // placeholder); the LOAD/REPS/DIST fields prefill their target (bright, editable).
    var timeDigits by remember(set?.id) { mutableStateOf(secondsToDigits(set?.timeSec)) }
    var reps by remember(set?.id) { mutableStateOf(intText(set?.reps ?: set?.targetReps)) }
    var load by remember(set?.id) {
        mutableStateOf(
            kgPlain(
                set?.loadKg ?: set?.targetLoadKg,
                unit,
            ),
        )
    }
    var dist by remember(set?.id) {
        mutableStateOf(
            intText(
                set?.distanceM ?: set?.targetDistanceM,
            ),
        )
    }
    val logged = set?.timeSec != null

    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).background(GlassFill)
            .border(1.dp, GlassBorder, shape).padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        // Header: icon + name + STATION n + ✓ confirm + ✕ remove.
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                stationIcon(item.segmentKey),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                item.exerciseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (stationNumber != null) {
                Text(
                    "STATION $stationNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.outline,
                )
            }

            ConfirmChip(logged) {
                if (set != null) {
                    val secs = digitsToSeconds(timeDigits)
                    onUpdate(
                        set.copy(
                            timeSec = secs.takeIf { it > 0 } ?: set.timeSec,
                            reps = if (second == SecondField.REPS) reps.toIntOrNull() else set.reps,
                            loadKg = if (second == SecondField.LOAD) {
                                load.toDoubleOrNull()
                                    ?.let { Units.toKg(it, unit) }
                            } else {
                                set.loadKg
                            },
                            distanceM = if (hasDist) dist.toIntOrNull() else set.distanceM,
                        ),
                    )
                }
            }
            DeleteButton(onRemove)
        }
        HorizontalDivider(color = GlassBorder)

        // Metric fields, equally divided.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            MetricField(
                "Time",
                timeDigits,
                { timeDigits = it.takeLast(6) },
                placeholder = "0:00",
                visualTransformation = ClockVisualTransformation,
                modifier = Modifier.weight(1f),
            )
            when (second) {
                SecondField.REPS -> MetricField(
                    "Reps",
                    reps,
                    { reps = it },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )

                SecondField.LOAD -> MetricField(
                    "Load ${Units.label(unit)}",
                    load,
                    { load = it },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )

                SecondField.NONE -> Unit
            }
            if (hasDist) {
                MetricField(
                    "Dist (m)",
                    dist,
                    { dist = it },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (standard != null) StandardView(standard)
    }
}

private enum class SecondField { REPS, LOAD, NONE }

/** Smaller confirm circle (station header). Red once the effort is logged. */
@Composable
private fun ConfirmChip(logged: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.size(24.dp).clip(CircleShape)
            .background(if (logged) colors.primary else colors.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            MindSetIcons.Check,
            contentDescription = "Confirm",
            tint = if (logged) colors.onPrimary else colors.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
    }
}

/** Remove this exercise/station from the session. */
@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.size(24.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            MindSetIcons.Delete,
            contentDescription = "Remove",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** Per-station glyph (SledPush/Farmers/Sandbag have no bespoke icon → nearest sensible fallback). */
private fun stationIcon(segmentKey: String?): androidx.compose.ui.graphics.vector.ImageVector = when {
    segmentKey == null -> MindSetIcons.Bolt
    "run" in segmentKey -> MindSetIcons.Run
    "ski" in segmentKey -> MindSetIcons.SkiErg
    "sled" in segmentKey -> MindSetIcons.SledPull
    "burpee" in segmentKey -> MindSetIcons.Burpee
    "rowing" in segmentKey -> MindSetIcons.Rowing
    "farmers" in segmentKey -> MindSetIcons.Dumbbell
    "sandbag" in segmentKey || "lunge" in segmentKey -> MindSetIcons.LowerBody
    "wall-ball" in segmentKey -> MindSetIcons.WallBall
    else -> MindSetIcons.Bolt
}

/** The reference "standard" as its own bordered, primary-tinted view with an info glyph. */
@Composable
private fun StandardView(standard: String) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .background(colors.primary.copy(alpha = 0.06f))
            .border(1.dp, colors.primary.copy(alpha = 0.25f), shape)
            .padding(horizontal = MaterialTheme.spacing.smd, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(
            MindSetIcons.Info,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            "Standard: $standard",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun EsBadge() {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.clip(CircleShape).background(colors.surfaceContainerHighest)
            .padding(horizontal = MaterialTheme.spacing.xs, vertical = MaterialTheme.spacing.xs),
    ) {
        Text("ES", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

// ── Set rows (metric-aware, ghost target → actual) ──────────────────────────────────────────────

@Composable
private fun SetRow(capture: CaptureFields, set: SetEntry, showSetLabel: Boolean, unit: WeightUnit, onUpdate: (SetEntry) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val logged = isLogged(capture, set)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        if (showSetLabel) {
            Text(
                "SET ${set.setNumber}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.width(44.dp),
            )
        }
        when (capture) {
            CaptureFields.WeightReps -> {
                var load by remember(set.id) {
                    mutableStateOf(
                        kgPlain(
                            set.loadKg ?: set.targetLoadKg,
                            unit,
                        ),
                    )
                }
                var reps by remember(set.id) { mutableStateOf(intText(set.reps ?: set.targetReps)) }
                StepperField(
                    load,
                    { load = it },
                    { load = stepText(load, it) },
                    Modifier.weight(1f),
                    caption = "Load (${Units.label(unit)})",
                )
                StepperField(
                    reps,
                    { reps = it },
                    { reps = stepText(reps, it) },
                    Modifier.weight(1f),
                    caption = "Reps",
                )
                ConfirmButton(logged) {
                    reps.toIntOrNull()?.let { r ->
                        onUpdate(
                            set.copy(
                                reps = r,
                                loadKg = load.toDoubleOrNull()?.let { Units.toKg(it, unit) },
                            ),
                        )
                    }
                }
            }

            CaptureFields.RepsOnly -> {
                var reps by remember(set.id) { mutableStateOf(intText(set.reps ?: set.targetReps)) }
                StepperField(
                    reps,
                    { reps = it },
                    { reps = stepText(reps, it) },
                    Modifier.weight(1f),
                    caption = "Reps",
                )
                ConfirmButton(logged) { reps.toIntOrNull()?.let { onUpdate(set.copy(reps = it)) } }
            }

            CaptureFields.Duration -> {
                var digits by remember(set.id) {
                    mutableStateOf(
                        secondsToDigits(
                            set.timeSec ?: set.targetTimeSec,
                        ),
                    )
                }
                MetricField(
                    "Time",
                    digits,
                    { digits = it.takeLast(6) },
                    placeholder = "0:00",
                    visualTransformation = ClockVisualTransformation,
                    modifier = Modifier.weight(1f),
                )
                ConfirmButton(logged) {
                    digitsToSeconds(digits).takeIf { it > 0 }
                        ?.let { onUpdate(set.copy(timeSec = it)) }
                }
            }

            CaptureFields.DistanceTime -> {
                var digits by remember(set.id) {
                    mutableStateOf(
                        secondsToDigits(
                            set.timeSec ?: set.targetTimeSec,
                        ),
                    )
                }
                var dist by remember(set.id) {
                    mutableStateOf(
                        intText(
                            set.distanceM ?: set.targetDistanceM,
                        ),
                    )
                }
                MetricField(
                    "Time",
                    digits,
                    { digits = it.takeLast(6) },
                    placeholder = "0:00",
                    visualTransformation = ClockVisualTransformation,
                    modifier = Modifier.weight(1f),
                )
                MetricField(
                    "Dist (m)",
                    dist,
                    { dist = it },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )
                ConfirmButton(logged) {
                    val secs = digitsToSeconds(digits)
                    if (secs > 0 || dist.toIntOrNull() != null) {
                        onUpdate(
                            set.copy(
                                timeSec = secs.takeIf { it > 0 },
                                distanceM = dist.toIntOrNull(),
                            ),
                        )
                    }
                }
            }

            CaptureFields.Calories -> {
                var cal by remember(set.id) {
                    mutableStateOf(
                        intText(
                            set.calories ?: set.targetCalories,
                        ),
                    )
                }
                MetricField(
                    "Cal",
                    cal,
                    { cal = it },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )
                ConfirmButton(logged) {
                    cal.toIntOrNull()?.let { onUpdate(set.copy(calories = it)) }
                }
            }
        }
    }
}

@Composable
private fun ConfirmButton(logged: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape)
            .background(if (logged) colors.primary else colors.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            MindSetIcons.Check,
            contentDescription = "Confirm",
            tint = if (logged) colors.onPrimary else colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun AddSetButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            MindSetIcons.Add,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(
            "Add Set".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
        )
    }
}

// ── Glass metric input (label top-left, value with dimmed placeholder) ───────────────────────────

@Composable
private fun MetricField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0",
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier.clip(shape).background(GlassFill).border(1.dp, GlassBorder, shape)
            .padding(horizontal = MaterialTheme.spacing.smd, vertical = MaterialTheme.spacing.sm),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
            ),
            cursorBrush = SolidColor(colors.primary),
            visualTransformation = visualTransformation,
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurfaceVariant.copy(alpha = 0.35f),
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val ClockVisualTransformation = VisualTransformation { text ->
    val digits = text.text.filter { it.isDigit() }
    val out = digitsToClock(digits)
    TransformedText(
        AnnotatedString(out),
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = out.length
            override fun transformedToOriginal(offset: Int): Int = digits.length
        },
    )
}

// ── Add-to-session sheet (Exercises + Stations) ─────────────────────────────────────────────────

@Composable
private fun AddToSessionSheet(stations: List<StationOption>, onBrowseExercises: () -> Unit, onPickStation: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.md)
            .padding(bottom = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Text(
            "Add to session".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )

        SheetRow(
            title = "Browse exercises",
            subtitle = "Search the full library",
            leading = MindSetIcons.Dumbbell,
            onClick = onBrowseExercises,
        )

        if (stations.isNotEmpty()) {
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(
                "Hyrox stations".uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.outline,
            )
            stations.forEach { station ->
                SheetRow(
                    title = station.name,
                    subtitle = station.standard,
                    leading = stationIcon(station.segmentKey),
                    onClick = { onPickStation(station.segmentKey) },
                )
            }
        }
    }
}

@Composable
private fun SheetRow(title: String, subtitle: String, leading: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier.fillMaxWidth().clip(shape).background(GlassFill).border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick).padding(MaterialTheme.spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                leading,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Add / Complete CTAs ─────────────────────────────────────────────────────────────────────────

@Composable
private fun AddCta(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick).padding(vertical = MaterialTheme.spacing.smd),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            MindSetIcons.Add,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(
            "Add Exercise or Station".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
        )
    }
}

/** One-tap seeding of a whole Hyrox race format (runs + stations) so the athlete only edits actuals. */
@Composable
private fun RaceQuickAdd(onPick: (HyroxVariant) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        Text(
            "Quick add race".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.outline,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            VariantChip("Full", Modifier.weight(1f)) { onPick(HyroxVariant.FULL) }
            VariantChip("1st Half", Modifier.weight(1f)) { onPick(HyroxVariant.FIRST_HALF) }
            VariantChip("2nd Half", Modifier.weight(1f)) { onPick(HyroxVariant.SECOND_HALF) }
            VariantChip("Halved", Modifier.weight(1f)) { onPick(HyroxVariant.HALVED) }
        }
    }
}

@Composable
private fun VariantChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier.clip(shape).background(GlassFill).border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick).padding(vertical = MaterialTheme.spacing.smd),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CompleteCta(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryButton(
        text = "Complete Session",
        enabled = true,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
            .navigationBarsPadding()
            .padding(MaterialTheme.spacing.md),
    )
}

// ── Metric helpers ──────────────────────────────────────────────────────────────────────────────

/** Whether a set already holds a logged actual (vs. a target-only ghost). */
private fun isLogged(capture: CaptureFields, s: SetEntry): Boolean = when (capture) {
    CaptureFields.WeightReps -> s.reps != null || s.loadKg != null
    CaptureFields.RepsOnly -> s.reps != null
    CaptureFields.Duration -> s.timeSec != null
    CaptureFields.DistanceTime -> s.timeSec != null || s.distanceM != null
    CaptureFields.Calories -> s.calories != null
}

private fun intText(v: Int?): String = v?.toString().orEmpty()

/** Nudge a numeric text value by [delta], clamped at 0. Empty → treated as 0. */
private fun stepText(current: String, delta: Int): String = ((current.toIntOrNull() ?: 0) + delta).coerceAtLeast(0).toString()

private fun kgPlain(kg: Double?, unit: WeightUnit): String {
    if (kg == null) return ""
    val v = Units.toDisplay(kg, unit)
    return if (v % 1.0 == 0.0) v.toInt().toString() else ((v * 10).toInt() / 10.0).toString()
}

/** Seconds → the raw digit buffer the ClockField edits (e.g. 150 → "230", 45 → "45"). */
private fun secondsToDigits(sec: Int?): String {
    if (sec == null || sec <= 0) return ""
    val m = sec / 60
    val s = sec % 60
    return if (m == 0) s.toString() else "$m${s.toString().padStart(2, '0')}"
}

private fun digitsToSeconds(digits: String): Int {
    val d = digits.filter { it.isDigit() }
    if (d.isEmpty()) return 0
    val ss = d.takeLast(2).toInt()
    val mm = d.dropLast(2).ifEmpty { "0" }.toInt()
    return mm * 60 + ss
}

private fun digitsToClock(digits: String): String {
    val d = digits.filter { it.isDigit() }
    if (d.isEmpty()) return ""
    val ss = d.takeLast(2).padStart(2, '0')
    val mm = d.dropLast(2).ifEmpty { "0" }
    return "$mm:$ss"
}

private fun formatDateTime(millis: Long): String = SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()).format(Date(millis))
