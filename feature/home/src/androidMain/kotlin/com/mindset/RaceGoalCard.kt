package com.mindset

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.mindset.presentation.Widget

@Composable
fun RaceGoalCard(widget: Widget.RaceGoalWidget, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing

    val transition = rememberInfiniteTransition(label = "race-goal-glow")
    val glow by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            tween(
                2100,
                easing = LinearEasing,
            ),
            RepeatMode.Reverse,
        ),
        label = "race-goal-glow-alpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .homeGlass(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.primary.copy(alpha = glow), Color.Transparent),
                            center = Offset(x = size.width, y = 0f),
                            radius = size.maxDimension * 0.7f,
                        ),
                    )
                }
                .padding(horizontal = MaterialTheme.spacing.smd, vertical = MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = "RACE DAY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier,
            )

            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = widget.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (widget.daysUntil != null) {
                Spacer(Modifier.height(spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = widget.daysUntil.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.onSurface,
                        modifier = Modifier.alignByBaseline(),
                    )

                    Text(
                        text = if (widget.daysUntil == 1) "DAY" else "DAYS",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }
        }
    }
}
