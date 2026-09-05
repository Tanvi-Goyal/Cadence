package com.mindset

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.PrimaryButton
import com.mindset.components.SecondaryButton
import com.mindset.domain.ActiveWorkoutController
import com.mindset.helpers.UIHelper
import com.mindset.icons.ChevronRight
import com.mindset.icons.Close
import com.mindset.model.ActiveWorkout
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxStationType
import kotlinx.coroutines.launch

/*
 * The live HYROX workout overlay. A standard Material 3 [ModalBottomSheet] — it supplies the scrim,
 * drag handle, swipe-to-dismiss, predictive back and inset handling, so none of that is hand-rolled
 * here. Hosted at the app root (see MainActivity) so it floats over any screen and survives navigation.
 *
 * Swiping the sheet down MINIMIZES the race (it keeps running, and the pill brings it back); ending a
 * race stays an explicit ✕, because a stray swipe must never destroy a workout in progress.
 *
 * The "stacked deck" shows the current step in full plus the next two dimmed behind it, advancing via
 * an AnimatedContent keyed on the step index. The clock ticks ~5×/sec — only [TimerReadout] reads the
 * changing elapsed values, so the deck (keyed on the stable index) skips recomposition on ticks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutHost(controller: ActiveWorkoutController) {
    val workout by controller.state.collectAsStateWithLifecycle()
    val expanded by controller.expanded.collectAsStateWithLifecycle()

    // Read the scheme inside the theme — outside it, MaterialTheme returns the light scheme.
    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        // Let the sheet animate out before the controller state flips, so the button-driven exits
        // slide away like the swipe does instead of vanishing.
        val hideThen: (() -> Unit) -> Unit = { action ->
            scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
        }

        val live = workout
        if (live != null && expanded) {
            ModalBottomSheet(
                onDismissRequest = controller::collapse,
                sheetState = sheetState,
                containerColor = colors.surfaceContainerLow,
            ) {
                ActiveWorkoutSheet(
                    workout = live,
                    onMinimize = { hideThen(controller::collapse) },
                    onClose = { hideThen(controller::dismiss) },
                    onReset = controller::reset,
                    onPauseResume = { if (live.paused) controller.resume() else controller.pause() },
                    onNext = controller::next,
                )
            }
        }

        // Keep the last non-null workout so the pill still has content during its exit slide.
        var last by remember { mutableStateOf<ActiveWorkout?>(null) }
        if (workout != null) last = workout

        // Minimized: a compact pill so a running race is always reachable again. This lives in the
        // host, not on Home, because the only screen a race is started from (the Hyrox sim detail)
        // has no bottom nav — without it, minimizing there would strand a running workout with no
        // route back. The host is a sibling of the NavHost and deliberately cannot read the route,
        // so it can't (and shouldn't) gate itself per-screen.
        Box(Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = workout != null && !expanded,
                enter = slideInVertically(
                    tween(240),
                ) { it } + fadeIn(tween(240)),
                exit = slideOutVertically(
                    tween(200),
                ) { it } + fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                last?.let { MinimizedPill(it, onClick = controller::expand) }
            }
        }
    }
}

@Composable
private fun ActiveWorkoutSheet(
    workout: ActiveWorkout,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onPauseResume: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .navigationBarsPadding()
            .padding(horizontal = MaterialTheme.spacing.md)
            .padding(bottom = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Header(
            index = workout.currentIndex,
            total = workout.totalSteps,
            finished = workout.finished,
            onMinimize = onMinimize,
            onClose = onClose,
        )

        if (workout.finished) {
            FinishedSummary(
                totalMs = workout.totalElapsedMs,
                steps = workout.totalSteps,
                onClose = onClose,
            )
        } else {
            TimerReadout(
                totalMs = workout.totalElapsedMs,
                splitMs = workout.splitElapsedMs,
                paused = workout.paused,
            )
            Deck(steps = workout.steps, currentIndex = workout.currentIndex)
            Spacer(Modifier.weight(1f))
            Controls(
                paused = workout.paused,
                isLast = workout.currentIndex >= workout.totalSteps - 1,
                onReset = onReset,
                onPauseResume = onPauseResume,
                onNext = onNext,
            )
        }
    }
}

@Composable
private fun Header(
    index: Int,
    total: Int,
    finished: Boolean,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (finished) "Complete".uppercase()
            else "Step ${index + 1} of $total".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            if (!finished) {
                FilledTonalIconButton(onClick = onMinimize) {
                    Icon(
                        imageVector = MindSetIcons.ChevronRight,
                        contentDescription = "Minimize",
                        modifier = Modifier.size(12.dp).rotate(90f), // chevron-right → chevron-down
                    )
                }
            }
            // Close ends the live race (a finished one still stays in History).
            FilledTonalIconButton(onClick = onClose) {
                Icon(
                    imageVector = MindSetIcons.Close,
                    contentDescription = "Close workout",
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun TimerReadout(totalMs: Long, splitMs: Long, paused: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(
            text = formatClock(totalMs),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = if (paused) colors.onSurfaceVariant else colors.onSurface,
        )
        Text(
            text = if (paused) "Paused".uppercase()
            else "Split ${formatClock(splitMs)}".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (paused) colors.error else colors.primary,
        )
    }
}

@Composable
private fun Deck(steps: List<HyroxStationModel>, currentIndex: Int) {
    AnimatedContent(
        targetState = currentIndex,
        transitionSpec = {
            (slideInVertically(
                tween(360),
            ) { it / 2 } + fadeIn(tween(360))) togetherWith
                (slideOutVertically(
                    tween(360),
                ) { -it / 2 } + fadeOut(tween(220)))
        },
        label = "deck",
    ) { idx ->
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd)) {
            steps.getOrNull(idx)?.let { CurrentCard(it) }
            steps.getOrNull(idx + 1)?.let { NextCard(it, alpha = 0.5f) }
            steps.getOrNull(idx + 2)?.let { NextCard(it, alpha = 0.28f) }
        }
    }
}

@Composable
private fun CurrentCard(step: HyroxStationModel) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val isRun = step.stationType == HyroxStationType.RUN
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
                imageVector = UIHelper.stationIcon(step.segmentKey),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (step.detail.isNotBlank()) {
                    Text(
                        text = step.detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
        HorizontalDivider(color = GlassBorder)
        Text(
            text = step.value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isRun) colors.primary else colors.onSurface,
        )
    }
}

@Composable
private fun NextCard(step: HyroxStationModel, alpha: Float) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .padding(MaterialTheme.spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Icon(
            imageVector = UIHelper.stationIcon(step.segmentKey),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = step.title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = step.value,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun Controls(paused: Boolean, isLast: Boolean, onReset: () -> Unit, onPauseResume: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryButton(
            text = "Reset",
            onClick = onReset,
            modifier = Modifier.weight(1f),
        )
        SecondaryButton(
            text = if (paused) "Resume" else "Pause",
            onClick = onPauseResume,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            text = if (isLast) "Finish" else "Next",
            enabled = true,
            onClick = onNext,
            modifier = Modifier.weight(1.4f),
        )
    }
}

@Composable
private fun ColumnScope.FinishedSummary(totalMs: Long, steps: Int, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Spacer(Modifier.weight(1f))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Text(
            text = "Workout Completed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
        Spacer(Modifier.size(MaterialTheme.spacing.smd))
        Text(
            text = "Total time".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = formatClock(totalMs),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
        Text(
            text = "$steps steps completed",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.weight(1f))
    PrimaryButton(
        text = "Done",
        enabled = true,
        onClick = onClose,
        modifier = Modifier.fillMaxWidth(),
    )
}
