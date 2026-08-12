package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindset.icons.Check
import com.mindset.presentation.Widget

@Composable
fun WeeklyPerformanceCard(
    widget: Widget.PerformanceWidget,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("This week")

            Text(
                text = if (widget.sessionCount == 1) "1 session"
                else "${widget.sessionCount} sessions",
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            week.forEach { cell -> DayDot(cell) }
        }
    }
}

@Composable
private fun DayDot(cell: WeekCell) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Text(
            text = cell.letter,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )

        val base = Modifier.size(36.dp)
            .clip(MaterialTheme.shapes.small)

        val boxModifier = when (cell.state) {
            DayState.TODAY -> base.background(colors.primary)
            DayState.TRAINED -> base.border(1.dp, colors.primary, MaterialTheme.shapes.small)
            DayState.IDLE -> base.background(GlassFill)
        }

        Box(contentAlignment = Alignment.TopEnd) {
            Box(boxModifier, contentAlignment = Alignment.Center) {
                Text(
                    text = cell.dayOfMonth,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (cell.state == DayState.TODAY)
                        FontWeight.Bold else FontWeight.Normal,
                    color = when (cell.state) {
                        DayState.TODAY -> colors.onPrimary
                        DayState.TRAINED -> colors.onSurface
                        DayState.IDLE -> colors.onSurfaceVariant
                    },
                )
            }

            if (cell.state == DayState.TRAINED) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MindSetIcons.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(10.dp),
                    )
                }
            }
        }
    }
}
