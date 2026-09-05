package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.icons.ChevronRight
import com.mindset.model.ActiveWorkout
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Home's live-race card: a minimized race stays visible and controllable without reopening the full
 * timer sheet. Tapping the card re-expands the sheet; the inline pills drive the same app-scoped
 * controller the sheet does, so the two surfaces can never disagree.
 *
 * **Recomposition.** The race clock ticks ~5×/sec, so this is split in two on purpose:
 *  - this composable reads only [LiveWorkoutData] — the fields that change per *step*, not per tick — via a
 *    derived, de-duplicated flow, so the title, step label, chevron and all three pills are untouched
 *    between ticks;
 *  - [LiveClock] collects the raw flow itself, so it is the only thing that recomposes at 5 Hz.
 *
 * The widget list above it is insulated separately: `HomeViewModel.liveWorkoutSlot()` reduces the
 * controller flow to a presence Boolean before de-duplicating, so `HomeUiState` re-emits twice per
 * race rather than five times a second.
 */
@Composable
fun LiveWorkoutSlot(
    activeWorkout: StateFlow<ActiveWorkout?>,
    onExpand: () -> Unit,
    onReset: () -> Unit,
    onTogglePause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val chromeFlow = remember(activeWorkout) {
        activeWorkout.map { it?.toChrome() }.distinctUntilChanged()
    }
    val chrome by chromeFlow.collectAsStateWithLifecycle(
        initialValue = activeWorkout.value?.toChrome(),
    )
    val card = chrome ?: return

    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Text(
            text = "In progress".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
        )
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(shape)
                .background(GlassFill)
                .border(1.dp, colors.primary.copy(alpha = 0.5f), shape)
                .clickable(onClick = onExpand)
                .padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = card.stepLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }

                LiveClock(activeWorkout)
                Icon(
                    MindSetIcons.ChevronRight,
                    contentDescription = "Open workout",
                    tint = colors.primary,
                    modifier = Modifier,
                )
            }

            if (!card.finished) {
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    WorkoutActionPill(
                        "Reset",
                        primary = false,
                        weight = 1f,
                        onClick = onReset,
                    )

                    WorkoutActionPill(
                        text = if (card.paused) "Resume" else "Pause",
                        primary = false,
                        weight = 1f,
                        onClick = onTogglePause,
                    )

                    WorkoutActionPill(
                        text = if (card.isLast) "Finish" else "Next",
                        primary = true,
                        weight = 1.4f,
                        onClick = onNext,
                    )
                }
            }
        }
    }
}

@Immutable
private data class LiveWorkoutData(
    val title: String,
    val stepLabel: String,
    val paused: Boolean,
    val finished: Boolean,
    val isLast: Boolean,
)

private fun ActiveWorkout.toChrome() = LiveWorkoutData(
    title = current?.title ?: "Workout",
    stepLabel = "Step ${currentIndex + 1} of $totalSteps",
    paused = paused,
    finished = finished,
    isLast = currentIndex >= totalSteps - 1,
)

/**
 * The count-up clock — the ONLY part of this card that recomposes on each ~200 ms tick. It collects
 * the controller flow itself rather than taking elapsed as a parameter, so the invalidation stops
 * here instead of propagating up through the card.
 */
@Composable
private fun LiveClock(activeWorkout: StateFlow<ActiveWorkout?>) {
    val colors = MaterialTheme.colorScheme
    val workout by activeWorkout.collectAsStateWithLifecycle()
    val live = workout ?: return
    val dimmed = live.paused && !live.finished

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = formatClock(live.totalElapsedMs),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (dimmed) colors.onSurfaceVariant else colors.onSurface,
        )
        Text(
            text = when {
                live.finished -> "DONE"
                live.paused -> "PAUSED"
                else -> "LIVE"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (dimmed) colors.error else colors.primary,
        )
    }
}

@Composable
private fun RowScope.WorkoutActionPill(
    text: String,
    primary: Boolean,
    weight: Float,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val base = Modifier.weight(weight)
        .clip(CircleShape)
    val styled = if (primary) {
        base.background(colors.primary)
    } else {
        base.background(GlassFill).border(1.dp, GlassBorder, CircleShape)
    }
    Box(
        styled
            .padding(
                horizontal = MaterialTheme.spacing.xs,
                vertical = MaterialTheme.spacing.sm,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal,
            color = if (primary) colors.onPrimary else colors.onSurface,
        )
    }
}
