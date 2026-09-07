package com.mindset.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons
import com.mindset.icons.Timer
import com.mindset.model.ActiveWorkout
import com.mindset.spacing

/**
 * The minimized live-race affordance: tap to reopen the sheet. Reads `totalElapsedMs`, so it
 * recomposes on each ~200 ms tick — but whoever hosts it already collects that flow at the same rate,
 * so this adds one `Text` to an existing tick rather than a new tick source.
 *
 * Built on [Surface]'s clickable overload rather than a hand-rolled `background + clip + clickable`,
 * so it gets the ripple, elevation and button semantics for free.
 */
@Composable
fun LiveWorkoutPill(workout: ActiveWorkout, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.smd,
                vertical = MaterialTheme.spacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Icon(
                imageVector = MindSetIcons.Timer,
                contentDescription = "Reopen workout",
                tint = colors.primary,
                modifier = Modifier.size(PillIconSize),
            )
            Text(
                text = formatClockMs(workout.totalElapsedMs),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${workout.currentIndex + 1}/${workout.totalSteps}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

private val PillIconSize = 16.dp

/**
 * Which surface currently owns the minimized pill.
 *
 * [MindSetTopBar] renders the pill inline, so a minimized race sits in the toolbar rather than
 * floating over the bottom tabs. Screens with no toolbar (the Hyrox sim detail a race is *started*
 * from, the log-workout screen) still need a route back to a running race, so the app-root
 * `ActiveWorkoutHost` keeps a floating fallback — and it is a SIBLING of the NavHost, so a
 * CompositionLocal can't reach it. This tiny app-scoped registry is the coordination: the top bar
 * claims the slot while it is composed, the fallback renders only when nothing has claimed it.
 *
 * A count, not a Boolean: two top bars are briefly composed at once during a nav transition, and a
 * Boolean would be cleared by the outgoing one's `onDispose` after the incoming one set it.
 */
object LiveWorkoutPillSlot {
    private var claims by mutableIntStateOf(0)

    val claimed: Boolean get() = claims > 0

    internal fun claim() {
        claims++
    }

    internal fun release() {
        claims--
    }
}
