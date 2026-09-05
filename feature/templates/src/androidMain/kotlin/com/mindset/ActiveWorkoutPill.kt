package com.mindset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindset.icons.Timer
import com.mindset.model.ActiveWorkout

/**
 * The minimized live-race affordance: tap to reopen the sheet. Reads `totalElapsedMs`, so it
 * recomposes on each ~200 ms tick — but the host already collects that flow at the same rate, so this
 * adds one `Text` to an existing tick rather than a new tick source.
 *
 * Built on [Surface]'s clickable overload rather than a hand-rolled `background + clip + clickable`,
 * so it gets the ripple, elevation and button semantics for free.
 */
@Composable
internal fun MinimizedPill(workout: ActiveWorkout, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(MaterialTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.md,
                vertical = MaterialTheme.spacing.smd,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Icon(
                imageVector = MindSetIcons.Timer,
                contentDescription = "Reopen workout",
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = formatClock(workout.totalElapsedMs),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Step ${workout.currentIndex + 1}/${workout.totalSteps}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
