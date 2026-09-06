package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindset.icons.Check

/**
 * One day in a training calendar: a rounded tile carrying the day-of-month, plus a check badge on a
 * day that was trained. Three states only ([DayState]), so Home's week strip and History's 30-day
 * grid are the same mark at different counts — promoted here from `:feature:home` for exactly that.
 */
@Composable
fun CalendarDayDot(cell: WeekCell, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        val base = Modifier.size(DotSize).clip(MaterialTheme.shapes.small)

        val boxModifier = when (cell.state) {
            DayState.TODAY -> base.background(colors.primary)
            DayState.TRAINED -> base
                .background(colors.primary.copy(alpha = TrainedFillAlpha))
                .border(1.dp, colors.primary, MaterialTheme.shapes.small)

            DayState.IDLE -> base.background(GlassFill)
        }

        Box(contentAlignment = Alignment.TopEnd) {
            Box(boxModifier, contentAlignment = Alignment.Center) {
                Text(
                    text = cell.dayOfMonth,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (cell.state == DayState.TODAY) FontWeight.Bold else FontWeight.Normal,
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
                        .offset(x = BadgeOffset, y = -BadgeOffset)
                        .size(BadgeSize)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MindSetIcons.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(BadgeIconSize),
                    )
                }
            }
        }
    }
}

// Fixed size so a 7-per-row grid lines up across screens; the row spreads the leftover width.
private val DotSize = 30.dp
private val BadgeOffset = 4.dp
private val BadgeSize = 16.dp
private val BadgeIconSize = 8.dp
private const val TrainedFillAlpha = 0.18f
