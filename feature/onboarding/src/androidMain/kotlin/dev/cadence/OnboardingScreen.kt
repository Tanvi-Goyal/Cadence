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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.icons.Add
import dev.cadence.icons.ChevronRight
import dev.cadence.presentation.Gender
import dev.cadence.presentation.OnboardingStep
import dev.cadence.presentation.OnboardingUiState
import dev.cadence.presentation.OnboardingViewModel
import dev.cadence.presentation.RaceFormat
import dev.cadence.presentation.Tier
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * First-run onboarding (Figma 0:3 / 0:105). A data-driven flow: the scaffold (brand lockup, STEP x/N
 * caption, progress bar, bottom buttons) is shared across steps; the step content switches on
 * [OnboardingStep]. Adding a step = extend the enum + add a content branch here. All fields live in
 * [OnboardingViewModel]; on Complete it persists the AthleteProfile and the screen leaves onboarding.
 */

private val Gutter = 24.dp
private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Leave onboarding once the profile is persisted.
    androidx.compose.runtime.LaunchedEffect(state.done) { if (state.done) onComplete() }

    CadenceTheme {
        val colors = MaterialTheme.colorScheme
        Box(Modifier.fillMaxSize().background(colors.background)) {
            OnboardingBackdrop()
            Column(
                Modifier
                    .fillMaxSize()
                    // safeDrawing = max(systemBars, ime, cutout) per edge, so the bottom follows the
                    // keyboard (animated) without double-counting the nav bar. Paired with the
                    // activity's adjustResize so the window resizes in sync instead of panning.
                    .safeDrawingPadding()
                    .padding(horizontal = Gutter),
            ) {
                Spacer(Modifier.height(24.dp))
                // Logo only (no wordmark) — the wordmark lives on the splash; onboarding shows the
                // glyph then the steps.
                BrandLockup(
                    modifier = Modifier.fillMaxWidth(),
                    logoSize = 60.dp,
                    showWordmark = false,
                )
                Spacer(Modifier.height(24.dp))
                StepIndicator(stepIndex = state.stepIndex, stepCount = state.stepCount)
                Spacer(Modifier.height(28.dp))

                // Scrollable step content; the bottom buttons stay pinned below.
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (state.step) {
                        OnboardingStep.ATHLETE_PROFILE -> AthleteProfileStep(state, viewModel)
                        OnboardingStep.RACE_CONFIG -> RaceConfigStep(state, viewModel)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                BottomButtons(state, viewModel)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Backdrop ────────────────────────────────────────────────────────────────────────────────────

/** Obsidian base + a subtle warm radial glow near the top-center (Figma 0:3). */
@Composable
private fun OnboardingBackdrop() {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .height(360.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(colors.primary.copy(alpha = 0.10f), Color.Transparent),
                ),
            ),
    )
}

// ── Header bits ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(stepIndex: Int, stepCount: Int) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "STEP %02d/%02d".format(stepIndex + 1, stepCount),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(stepCount) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (i <= stepIndex) colors.primary else colors.surfaceContainerHigh),
                )
            }
        }
    }
}

@Composable
private fun StepHeading(title: String, subtitle: String) {
    val colors = MaterialTheme.colorScheme
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
    }
}

// ── Step 1: Athlete Profile ───────────────────────────────────────────────────────────────────

@Composable
private fun AthleteProfileStep(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column {
        StepHeading("Athlete Profile", "Define your physical baseline for precise programming.")
        FieldLabel("Full Name")
        GlassTextField(
            value = state.fullName,
            onValueChange = vm::onFullName,
            placeholder = "e.g. Alex Sterling",
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StepperField(
                label = "Bodyweight (kg)",
                value = state.bodyweightKg,
                onValueChange = vm::onBodyweight,
                onDecrement = { vm.stepBodyweight(-1) },
                onIncrement = { vm.stepBodyweight(+1) },
                modifier = Modifier.weight(1f),
            )
            StepperField(
                label = "Height (in)",
                value = state.heightIn,
                onValueChange = vm::onHeight,
                onDecrement = { vm.stepHeight(-1) },
                onIncrement = { vm.stepHeight(+1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Step 2: Race Configuration ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RaceConfigStep(state: OnboardingUiState, vm: OnboardingViewModel) {
    val colors = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }

    Column {
        StepHeading("Race Configuration", "Set your sights on the finish line.")

        FieldLabel("Competition Date")
        GlassCard(onClick = { showDatePicker = true }) {
            Text(
                text = state.raceDateMillis?.let(::formatDate) ?: "mm / dd / yyyy",
                style = MaterialTheme.typography.bodyLarge,
                color = if (state.raceDateMillis != null) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
        Spacer(Modifier.height(20.dp))

        FieldLabel("Category")
        SegmentedSelector(
            options = listOf("Women", "Men"),
            selectedIndex = state.gender?.ordinal ?: -1,
            onSelect = { vm.onGender(Gender.entries[it]) },
        )
        Spacer(Modifier.height(20.dp))

        FieldLabel("Division")
        SegmentedSelector(
            options = listOf("Open", "Pro"),
            selectedIndex = state.tier?.ordinal ?: -1,
            onSelect = { vm.onTier(Tier.entries[it]) },
        )
        Spacer(Modifier.height(20.dp))

        FieldLabel("Format")
        SegmentedSelector(
            options = listOf("Singles", "Doubles", "Relay"),
            selectedIndex = state.format?.ordinal ?: -1,
            onSelect = { vm.onFormat(RaceFormat.entries[it]) },
        )
        Spacer(Modifier.height(20.dp))

        FieldLabel("Target Race City")
        GlassTextField(
            value = state.raceCity,
            onValueChange = vm::onCity,
            placeholder = "e.g., Stockholm",
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.raceDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.onRaceDate(pickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// ── Bottom navigation buttons ─────────────────────────────────────────────────────────────────

@Composable
private fun BottomButtons(state: OnboardingUiState, vm: OnboardingViewModel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.isFirst) {
            GhostButton(text = "Back", onClick = vm::onBack, modifier = Modifier.weight(1f))
        }
        PrimaryButton(
            text = if (state.isLast) "Complete Setup" else "Next Configuration",
            enabled = state.currentStepValid && !state.saving,
            onClick = { if (state.isLast) vm.onComplete() else vm.onNext() },
            modifier = Modifier.weight(if (state.isFirst) 1f else 2f),
        )
    }
}

// ── Reusable primitives ───────────────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
    )
}

/** The glass surface primitive — translucent fill + hairline border (design-v2 "glass"). */
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    var base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(GlassFill)
        .border(1.dp, GlassBorder, shape)
    if (onClick != null) base = base.clickable(onClick = onClick)
    Box(base.padding(horizontal = 16.dp, vertical = 18.dp)) { content() }
}

@Composable
private fun GlassTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    val colors = MaterialTheme.colorScheme
    GlassCard {
        Box {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StepperField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        FieldLabel(label)
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = colors.onSurface),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                "0",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.weight(1f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StepChevron(up = true, onClick = onIncrement)
                    Spacer(Modifier.height(6.dp))
                    StepChevron(up = false, onClick = onDecrement)
                }
            }
        }
    }
}

@Composable
private fun StepChevron(up: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(20.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CadenceIcons.ChevronRight,
            contentDescription = if (up) "Increment" else "Decrement",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(12.dp).rotate(if (up) -90f else 90f),
        )
    }
}

@Composable
private fun SegmentedSelector(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) colors.primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (active) colors.onPrimary else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val bg = if (enabled) colors.primary else colors.surfaceContainerHigh
    val fg = if (enabled) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.5f)
    Box(
        modifier
            .height(60.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier
            .height(60.dp)
            .clip(shape)
            .border(1.dp, GlassBorder, shape)
            .background(GlassFill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
