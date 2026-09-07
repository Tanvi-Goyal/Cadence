package com.mindset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.PrimaryButton
import com.mindset.components.SecondaryButton
import com.mindset.components.formatClockMs
import com.mindset.icons.ChevronRight
import com.mindset.model.ActiveWorkout
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Home's live-race card: a minimized race stays visible and controllable without reopening the full
 * timer sheet. Tapping the card re-expands the sheet; the inline controls drive the same app-scoped
 * controller the sheet does, so the two surfaces can never disagree.
 *
 * **Recomposition.** The race clock ticks ~5×/sec, so this is split in two on purpose:
 *  - this composable reads only [LiveWorkoutData] — the fields that change per *step*, not per tick — via a
 *    derived, de-duplicated flow, so the title, step label, chevron and all three buttons are untouched
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        MindSetSectionHeader("In progress", Modifier.fillMaxWidth())
        Column(
            modifier = Modifier.fillMaxWidth()
                .homeGlass(border = liveBorder())
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
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // labelMedium, not labelSmall: this is the same "STEP n OF m" string the expanded
                    // sheet's header shows, and the two surfaces describe one race.
                    Text(
                        text = card.stepLabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }

                LiveClock(activeWorkout)
                Icon(
                    MindSetIcons.ChevronRight,
                    contentDescription = "Open workout",
                    tint = colors.primary,
                    modifier = Modifier.size(ChevronSize),
                )
            }

            // The same three shared buttons, in the same 1 / 1 / 1.4 proportion, as the expanded
            // sheet's `Controls`. They drive the same controller, so a hand-rolled variant here could
            // only ever drift away from the surface it mirrors — and the previous local pill had
            // already drifted: pill-shaped instead of the shape token, a step down the type scale, and
            // `.clickable` applied AFTER `.padding`, which shrank each tap target to the text bounds.
            if (!card.finished) {
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    SecondaryButton(
                        text = "Reset",
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        text = if (card.paused) "Resume" else "Pause",
                        onClick = onTogglePause,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = if (card.isLast) "Finish" else "Next",
                        enabled = true,
                        onClick = onNext,
                        modifier = Modifier.weight(1.4f),
                    )
                }
            }
        }
    }
}

/** Matches the trailing chevron on Home's other tappable row (`BrowseTemplatesCard`). */
private val ChevronSize = 20.dp

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

    // Monospaced digits: proportional Inter re-measures the clock on every tick, so the readout (and
    // everything laid out beside it) shifts horizontally 5×/sec as digits change width. MonoFontFamily
    // is the design system's metric-readout face and gives every digit one advance. `remember`ed
    // because this composable is the 5 Hz one — an inline `.copy()` would allocate a TextStyle per tick.
    val base = MaterialTheme.typography.headlineSmall
    val clockStyle = remember(base) { base.copy(fontFamily = MonoFontFamily) }

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = formatClockMs(live.totalElapsedMs),
            style = clockStyle,
            color = if (dimmed) colors.onSurfaceVariant else colors.onSurface,
        )
        Text(
            text = when {
                live.finished -> "DONE"
                live.paused -> "PAUSED"
                else -> "LIVE"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (dimmed) colors.error else colors.primary,
        )
    }
}
