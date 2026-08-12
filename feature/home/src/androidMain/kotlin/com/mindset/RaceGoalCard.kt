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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
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
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = widget.title.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = widget.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )

            if (widget.daysUntil != null) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text(
                        text = widget.daysUntil.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Text(
                        text = if (widget.daysUntil == 1) "DAY" else "DAYS",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                Text(
                    text = "Until race day",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                Text(
                    text = "Until race day",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
