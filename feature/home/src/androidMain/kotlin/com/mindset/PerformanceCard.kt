package com.mindset

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mindset.presentation.Widget

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyPerformanceCard(
    widget: Widget.PerformanceWidget,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "performance-glow")
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
        label = "performance-glow-alpha",
    )

    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing

    val week = remember(widget) {
        buildCalendarWeeks(
            widget.todayEpochDay,
            widget.trainedEpochDays,
            weeks = 1,
        ).first()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .homeGlass()
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
            text = "This week",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )

        Text(
            text = if (widget.sessionCount == 1) "1 session"
            else "${widget.sessionCount} sessions",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(spacing.xs))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.smd),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            week.forEach { cell -> CalendarDayDot(cell) }
        }
    }
}
