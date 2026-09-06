package com.mindset

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.GlassTextField
import com.mindset.components.MindSetTopBar
import com.mindset.components.typeLabel
import com.mindset.domain.LoggedItemUi
import com.mindset.domain.Units
import com.mindset.helpers.UIHelper
import com.mindset.icons.Add
import com.mindset.icons.Check
import com.mindset.icons.Close
import com.mindset.icons.Delete
// TODO(phase2): restore with the "Browse exercises" sheet row below.
// import com.mindset.icons.Dumbbell
import com.mindset.icons.Info
import com.mindset.model.BottomNavTab
import com.mindset.model.HyroxVariant
import com.mindset.model.SessionType
import com.mindset.presentation.LogWorkoutViewModel
import com.mindset.presentation.StationDraft
import com.mindset.presentation.StationOption
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Log Session — the capture surface, used two ways:
 *  - **pushed** (from the quick-start FAB or a template), where it owns the whole screen and its own
 *    close affordance, and
 *  - **as the Log tab**, when [onTab] is non-null: it then wears the app's standard chrome (wordmark
 *    top bar + bottom nav) like every other tab, and drops the ✕ / back interception, because a tab
 *    root has nothing to close back to.
 *
 * The ViewModel is keyed on [sessionId]: the tab reuses one back-stack entry across sessions, so
 * without the key a completed session's VM would be handed to the next draft.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutScreen(
    sessionId: String,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onFinish: () -> Unit,
    onTab: ((BottomNavTab) -> Unit)? = null,
    viewModel: LogWorkoutViewModel = koinViewModel(key = sessionId) { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val standards by viewModel.stationStandards.collectAsStateWithLifecycle()
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var pendingVariant by remember { mutableStateOf<HyroxVariant?>(null) }

    val isTab = onTab != null
    val onClose = { viewModel.close(onBack) }
    if (!isTab) BackHandler(onBack = onClose)

    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        val numberedCards = remember(state.sections) {
            var stationNo = 0
            state.sections.flatMap { it.items }.map { item ->
                val key = item.segmentKey
                val n = if (key != null && "run" !in key) ++stationNo else null
                item to n
            }
        }

        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = colors.background,
            topBar = {
                if (onTab != null) MindSetTopBar(onProfileClick = { onTab(BottomNavTab.Profile) })
            },
            bottomBar = {
                Column {
                    if (onTab != null) BottomNavBar(current = BottomNavTab.Log, onTabClick = onTab)
                }
            },
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
                        onBack = if (isTab) null else onClose,
                        onNotesChange = viewModel::onNotesChange,
                        onCompleted = { viewModel.finish(onFinish) },
                    )
                }

                item(key = "add") { AddCta(onClick = { showAddSheet = true }) }

                if (stations.isNotEmpty()) {
                    item(key = "quickAdd") {
                        RaceQuickAdd(
                            onPick = { variant ->
                                if (state.sections.any { it.items.isNotEmpty() }) {
                                    pendingVariant = variant
                                } else {
                                    viewModel.addVariant(variant)
                                }
                            },
                        )
                    }
                }

                items(
                    numberedCards,
                    key = {
                        it.first.loggedItemId
                    },
                ) { (item, n) ->
                    val set = item.sets.firstOrNull()
                    if (item.segmentKey != null && set != null) {
                        val setId = set.id
                        StationCard(
                            item = item,
                            stationNumber = n,
                            standard = standards[item.segmentKey],
                            draft = drafts[setId] ?: StationDraft(),
                            onTime = { viewModel.onTimeChange(setId, it) },
                            onReps = { viewModel.onRepsChange(setId, it) },
                            onLoad = { viewModel.onLoadChange(setId, it) },
                            onDist = { viewModel.onDistChange(setId, it) },
                            onConfirm = { viewModel.confirmStation(setId) },
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

        pendingVariant?.let { variant ->
            ReplaceRaceDialog(
                variant = variant,
                loggedCount = state.sections.sumOf { it.items.size },
                onConfirm = {
                    viewModel.addVariant(variant, replaceExisting = true)
                    pendingVariant = null
                },
                onDismiss = { pendingVariant = null },
            )
        }
    }
}

@Composable
private fun ReplaceRaceDialog(
    variant: HyroxVariant,
    loggedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Replace current log?") },
        text = {
            Text(
                "Quick-adding ${variant.label} clears the $loggedCount " + "${if (loggedCount == 1) "entry" else "entries"} already in this session.",
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Replace") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep") } },
        containerColor = colors.surfaceContainerHigh,
    )
}

@Composable
private fun Header(
    sessionType: SessionType,
    startedAtMillis: Long,
    notes: String,
    sessionId: String,
    onBack: (() -> Unit)?,
    onNotesChange: (String) -> Unit,
    onCompleted: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var noteText by rememberSaveable(sessionId) {
        mutableStateOf(notes)
    }

    LaunchedEffect(notes) {
        noteText = notes
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f),
                ) {
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

                CompleteCta(onClick = { onCompleted() })
            }

            if (onBack != null) {
                Box(
                    Modifier.size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHigh)
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

@Composable
private fun StationCard(
    item: LoggedItemUi,
    stationNumber: Int?,
    standard: String?,
    draft: StationDraft,
    onTime: (String) -> Unit,
    onReps: (String) -> Unit,
    onLoad: (String) -> Unit,
    onDist: (String) -> Unit,
    onConfirm: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    val shape = MaterialTheme.shapes.medium
    val set = item.sets.firstOrNull()

    val second: SecondField = when {
        set?.targetReps != null -> SecondField.REPS
        set?.targetLoadKg != null -> SecondField.LOAD
        else -> SecondField.NONE
    }
    val hasDist = set?.targetDistanceM != null
    val logged = set?.timeSec != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                UIHelper.stationIcon(item.segmentKey),
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

            ConfirmChip(logged, onConfirm)
            DeleteButton(onRemove)
        }
        HorizontalDivider(color = GlassBorder)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            MetricField(
                "Time (mins)",
                draft.timeDigits,
                onTime,
                placeholder = "0:00",
                visualTransformation = ClockVisualTransformation,
                modifier = Modifier.weight(1f),
            )
            when (second) {
                SecondField.REPS -> MetricField(
                    "Reps",
                    draft.reps,
                    onReps,
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )

                SecondField.LOAD -> MetricField(
                    "Load ${Units.label(unit)}",
                    draft.load,
                    onLoad,
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )

                SecondField.NONE -> Unit
            }
            if (hasDist) {
                MetricField(
                    "Dist (m)",
                    draft.dist,
                    onDist,
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (standard != null) StandardView(standard)
    }
}

private enum class SecondField { REPS, LOAD, NONE }

@Composable
private fun ConfirmChip(logged: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
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

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
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

@Composable
private fun StandardView(standard: String) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier.fillMaxWidth().clip(shape).background(colors.primary.copy(alpha = 0.06f))
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

@Composable
private fun AddToSessionSheet(
    stations: List<StationOption>,
    onBrowseExercises: () -> Unit,
    onPickStation: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.md).padding(bottom = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Text(
            "Add to session".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )

        // TODO(phase2): re-enable, cut from v1 scope — the exercise catalog browser. v1 logs Hyrox
        // stations only. Restoring this also needs the ExercisePicker/ExerciseDetail routes in
        // MindSetNavHost and the PICKED_EXERCISE handback in LoggingNavGraph.
//        SheetRow(
//            title = "Browse exercises",
//            subtitle = "Search the full library",
//            leading = MindSetIcons.Dumbbell,
//            onClick = onBrowseExercises,
//        )

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
                    leading = UIHelper.stationIcon(station.segmentKey),
                    onClick = { onPickStation(station.segmentKey) },
                )
            }
        }
    }
}

@Composable
private fun SheetRow(
    title: String,
    subtitle: String,
    leading: ImageVector,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.smd),
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

@Composable
private fun AddCta(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium).clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.smd),
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
            "Add Station".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
        )
    }
}

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
            HyroxVariant.entries.forEach { variant ->
                VariantChip(variant.label, Modifier.weight(1f)) { onPick(variant) }
            }
        }
    }
}

/** Chip caption for a variant — also the noun the replace dialog names. */
private val HyroxVariant.label: String
    get() = when (this) {
        HyroxVariant.FULL -> "Full"
        HyroxVariant.FIRST_HALF -> "1st Half"
        HyroxVariant.SECOND_HALF -> "2nd Half"
        HyroxVariant.HALVED -> "Halved"
    }

@Composable
private fun VariantChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.smd),
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
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier.clip(shape).border(1.dp, GlassBorder, shape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(
                vertical = MaterialTheme.spacing.smd,
                horizontal = MaterialTheme.spacing.smd,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Complete Session",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
    }
}

private fun digitsToClock(digits: String): String {
    val d = digits.filter { it.isDigit() }
    if (d.isEmpty()) return ""
    val ss = d.takeLast(2).padStart(2, '0')
    val mm = d.dropLast(2).ifEmpty { "0" }
    return "$mm:$ss"
}

private fun formatDateTime(millis: Long): String = SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()).format(Date(millis))
