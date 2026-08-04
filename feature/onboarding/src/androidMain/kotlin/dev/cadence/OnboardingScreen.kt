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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.components.FieldLabel
import dev.cadence.components.GlassCard
import dev.cadence.components.GlassTextField
import dev.cadence.components.PrimaryButton
import dev.cadence.components.SecondaryButton
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

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onComplete() }

    CadenceTheme {
        val colors = MaterialTheme.colorScheme
        Box(Modifier.fillMaxSize().background(colors.background)) {
            OnboardingBackdrop()
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = Dimens.Padding.lg),
            ) {
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                AppBrand(
                    modifier = Modifier.fillMaxWidth(),
                    logoSize = 60.dp,
                    showWordmark = false,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.lg))
                StepIndicator(stepIndex = state.stepIndex, stepCount = state.stepCount)
                Spacer(Modifier.height(28.dp))

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (state.step) {
                        OnboardingStep.ATHLETE_PROFILE -> AthleteProfileStep(state, viewModel)
                        OnboardingStep.RACE_CONFIG -> RaceConfigStep(state, viewModel)
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.md))
                }

                BottomButtons(state, viewModel)
                Spacer(Modifier.height(MaterialTheme.spacing.md))
            }
        }
    }
}

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
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            repeat(stepCount) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(
                            if (i <= stepIndex) colors.primary
                            else colors.surfaceContainerHigh
                        ),
                )
            }
        }
    }
}

@Composable
private fun StepHeading(title: String, subtitle: String) {
    val colors = MaterialTheme.colorScheme
    Column {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(MaterialTheme.spacing.lg))
    }
}

@Composable
private fun AthleteProfileStep(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column {
        StepHeading(
            "Athlete Profile",
            "Define your physical baseline for precise programming."
        )
        FieldLabel("Full Name")
        GlassTextField(
            value = state.fullName,
            onValueChange = vm::onFullName,
            placeholder = "e.g. Alex Sterling",
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
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
                color = if (state.raceDateMillis != null) colors.onSurface
                else colors.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))

        FieldLabel("Category")
        SegmentedSelector(
            options = listOf("Women", "Men"),
            selectedIndex = state.gender?.ordinal ?: -1,
            onSelect = { vm.onGender(Gender.entries[it]) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))

        FieldLabel("Division")
        SegmentedSelector(
            options = listOf("Open", "Pro"),
            selectedIndex = state.tier?.ordinal ?: -1,
            onSelect = { vm.onTier(Tier.entries[it]) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))

        FieldLabel("Format")
        SegmentedSelector(
            options = listOf("Singles", "Doubles", "Relay"),
            selectedIndex = state.format?.ordinal ?: -1,
            onSelect = { vm.onFormat(RaceFormat.entries[it]) },
        )
        Spacer(Modifier.height(MaterialTheme.spacing.md))

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

@Composable
private fun BottomButtons(state: OnboardingUiState, vm: OnboardingViewModel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Padding.sm),
    ) {
        if (!state.isFirst) {
            SecondaryButton(text = "Back", onClick = vm::onBack, modifier = Modifier.weight(1f))
        }

        PrimaryButton(
            text = if (state.isLast) "Complete Setup" else "Next Configuration",
            enabled = state.currentStepValid && !state.saving,
            onClick = { if (state.isLast) vm.onComplete() else vm.onNext() },
            modifier = Modifier.weight(if (state.isFirst) 1f else 2f),
        )
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
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                "0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.weight(1f),
                )
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    StepChevron(up = true, onClick = onIncrement)
//                    Spacer(Modifier.height(Dimens.Padding.xxs))
//                    StepChevron(up = false, onClick = onDecrement)
//                }
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
            .padding(Dimens.Padding.xs),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) colors.primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = Dimens.Padding.xsm),
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

private fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
