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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.domain.ActiveWorkout
import com.mindset.domain.ActiveWorkoutController
import androidx.compose.ui.draw.rotate
import com.mindset.icons.Barbell
import com.mindset.icons.Burpee
import com.mindset.icons.ChevronRight
import com.mindset.icons.Close
import com.mindset.icons.Dumbbell
import com.mindset.icons.EngineRun
import com.mindset.icons.LowerBody
import com.mindset.icons.Rowing
import com.mindset.icons.SkiErg
import com.mindset.icons.SledPull
import com.mindset.icons.Timer
import com.mindset.icons.WallBall
import com.mindset.model.HyroxStation
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxStationType

/*
 * The live HYROX workout overlay — a premium bottom sheet that animates up when a workout starts and
 * observes the app-scoped [ActiveWorkoutController]. Hosted at the app root (see MainActivity) so it
 * floats over any screen and survives navigation.
 *
 * The "stacked deck" shows the current step in full plus the next two dimmed behind it; advancing
 * slides the whole deck up while the next card rises from below (AnimatedContent keyed on the step
 * index). The count-up clock ticks ~5×/sec — only the digit composables read the changing elapsed
 * values, so the deck (keyed on the stable step index) skips recomposition on ticks.
 */
@Composable
fun ActiveWorkoutHost(controller: ActiveWorkoutController) {
    val workout by controller.state.collectAsStateWithLifecycle()
    val expanded by controller.expanded.collectAsStateWithLifecycle()
    // The sheet shows only while a workout is running AND not minimized; when minimized, the Home
    // mini-card takes over (the workout keeps ticking either way).
    val visible = workout != null && expanded

    // Keep the last non-null workout so the sheet still has content to render during its exit slide.
    var last by remember { mutableStateOf<ActiveWorkout?>(null) }
    if (workout != null) last = workout

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.62f)))
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(360)) { it } + fadeIn(tween(360)),
            exit = slideOutVertically(tween(280)) { it } + fadeOut(tween(280)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            last?.let { HyroxTimerSheet(it, controller) }
        }
        // Minimized: a compact pill so a running race is always reachable again. This lives in the
        // host, not on Home, because the only screen a race is started from (the Hyrox sim detail)
        // has no bottom nav — without it, minimizing there would strand a running workout with no
        // route back. The host is a sibling of the NavHost and deliberately cannot read the route,
        // so it can't (and shouldn't) gate itself per-screen.
        AnimatedVisibility(
            visible = workout != null && !expanded,
            enter = slideInVertically(tween(240)) { it } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            last?.let { MinimizedPill(it, onClick = controller::expand) }
        }
    }
}

/**
 * The minimized live-race affordance: tap to reopen the sheet. Reads `totalElapsedMs`, so it
 * recomposes on each ~200 ms tick — but the host already collects that flow at the same rate, so
 * this adds one `Text` to an existing tick rather than a new tick source.
 */
@Composable
private fun MinimizedPill(workout: ActiveWorkout, onClick: () -> Unit) {
    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(MaterialTheme.spacing.md)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh)
                .clickable(onClick = onClick)
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Icon(
                MindSetIcons.Timer,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = formatClock(workout.totalElapsedMs),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Text(
                text = "Step ${workout.currentIndex + 1}/${workout.totalSteps}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HyroxTimerSheet(workout: ActiveWorkout, controller: ActiveWorkoutController) {
    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(colors.surfaceContainerLow)
                .border(
                    1.dp,
                    colors.outlineVariant.copy(alpha = 0.4f),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            // Grab handle
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.outlineVariant),
            )

            Spacer(Modifier.height(16.dp))
            Header(
                index = workout.currentIndex,
                total = workout.totalSteps,
                finished = workout.finished,
                onMinimize = controller::collapse,
                onClose = controller::dismiss,
            )

            if (workout.finished) {
                FinishedSummary(
                    totalMs = workout.totalElapsedMs,
                    steps = workout.totalSteps,
                    onClose = controller::dismiss,
                )
            } else {
                Spacer(Modifier.height(20.dp))
                TimerReadout(
                    totalMs = workout.totalElapsedMs,
                    splitMs = workout.splitElapsedMs,
                    paused = workout.paused,
                )
                Spacer(Modifier.height(24.dp))
                Deck(steps = workout.steps, currentIndex = workout.currentIndex)
                Spacer(Modifier.weight(1f))
                Controls(
                    paused = workout.paused,
                    isLast = workout.currentIndex >= workout.totalSteps - 1,
                    onReset = controller::reset,
                    onPauseResume = { if (workout.paused) controller.resume() else controller.pause() },
                    onNext = controller::next,
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun Header(index: Int, total: Int, finished: Boolean, onMinimize: () -> Unit, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(if (finished) "COMPLETE" else "STEP ${index + 1} OF $total")
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Minimize: keeps the workout running and hands off to the Home mini-card (only useful
            // while a workout is live, not after it finishes).
            if (!finished) {
                HeaderCircleButton(
                    icon = MindSetIcons.ChevronRight,
                    contentDescription = "Minimize",
                    onClick = onMinimize,
                    rotateDegrees = 90f, // chevron-right rotated into a chevron-down
                )
            }
            // Close: ends/clears the live workout (a finished one still stays in History).
            HeaderCircleButton(
                icon = MindSetIcons.Close,
                contentDescription = "Close workout",
                onClick = onClose,
            )
        }
    }
}

@Composable
private fun HeaderCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    rotateDegrees: Float = 0f,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(14.dp).rotate(rotateDegrees),
        )
    }
}

/** The count-up clock. Isolated so only these digits recompose on each ~200 ms tick. */
@Composable
private fun TimerReadout(totalMs: Long, splitMs: Long, paused: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = formatClock(totalMs),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = if (paused) colors.onSurfaceVariant else colors.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (paused) "PAUSED" else "SPLIT  ${formatClock(splitMs)}",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = if (paused) colors.error else colors.primary,
        )
    }
}

@Composable
private fun Deck(steps: List<HyroxStationModel>, currentIndex: Int) {
    AnimatedContent(
        targetState = currentIndex,
        transitionSpec = {
            (slideInVertically(tween(360)) { it / 2 } + fadeIn(tween(360))) togetherWith
                (slideOutVertically(tween(360)) { -it / 2 } + fadeOut(tween(220)))
        },
        label = "deck",
    ) { idx ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            steps.getOrNull(idx)?.let { CurrentCard(it) }
            steps.getOrNull(idx + 1)?.let { NextCard(it, alpha = 0.5f) }
            steps.getOrNull(idx + 2)?.let { NextCard(it, alpha = 0.28f) }
        }
    }
}

@Composable
private fun CurrentCard(step: HyroxStationModel) {
    val colors = MaterialTheme.colorScheme
    val isRun = step.stationType == HyroxStationType.RUN
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .border(
                1.dp,
                if (isRun) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant.copy(alpha = 0.4f),
                MaterialTheme.shapes.medium,
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlyphBadge(step, big = true)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    step.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                if (step.detail.isNotBlank()) {
                    Text(step.detail, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
        }
        Text(
            step.value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isRun) colors.primary else colors.onSurface,
        )
    }
}

@Composable
private fun NextCard(step: HyroxStationModel, alpha: Float) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBadge(step, big = false)
        Spacer(Modifier.width(12.dp))
        Text(
            step.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(step.value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun GlyphBadge(step: HyroxStationModel, big: Boolean) {
    val colors = MaterialTheme.colorScheme
    val isRun = step.stationType == HyroxStationType.RUN
    val boxSize = if (big) 48.dp else 36.dp
    val iconSize = if (big) 24.dp else 18.dp
    Box(
        Modifier
            .size(boxSize)
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = glyphIcon(step),
            contentDescription = null,
            tint = if (isRun) colors.primary else colors.surfaceTint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun Controls(paused: Boolean, isLast: Boolean, onReset: () -> Unit, onPauseResume: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedPill("Reset", Modifier.weight(1f), onReset)
        OutlinedPill(if (paused) "Resume" else "Pause", Modifier.weight(1f), onPauseResume)
        PrimaryPill(if (isLast) "Finish" else "Next", Modifier.weight(1.4f), onNext)
    }
}

@Composable
private fun OutlinedPill(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
    }
}

@Composable
private fun PrimaryPill(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onPrimary)
    }
}

@Composable
private fun ColumnScope.FinishedSummary(totalMs: Long, steps: Int, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Spacer(Modifier.height(40.dp))
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Workout Complete",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "TOTAL TIME",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            formatClock(totalMs),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$steps steps completed",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.weight(1f))
    PrimaryPill("Done", Modifier.fillMaxWidth(), onClose)
    Spacer(Modifier.height(20.dp))
}

private fun glyphIcon(step: HyroxStationModel): ImageVector = when (step.station) {
    HyroxStation.SKI_ERG -> MindSetIcons.SkiErg
    HyroxStation.SLED_PUSH -> MindSetIcons.Dumbbell
    HyroxStation.SLED_PULL -> MindSetIcons.SledPull
    HyroxStation.BURPEE_BROAD_JUMP -> MindSetIcons.Burpee
    HyroxStation.ROWING -> MindSetIcons.Rowing
    HyroxStation.FARMERS_CARRY -> MindSetIcons.Barbell
    HyroxStation.SANDBAG_LUNGES -> MindSetIcons.LowerBody
    HyroxStation.WALL_BALLS -> MindSetIcons.WallBall
    null -> MindSetIcons.EngineRun // runs
}
