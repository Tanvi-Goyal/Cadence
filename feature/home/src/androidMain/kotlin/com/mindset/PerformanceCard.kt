package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@OptIn(ExperimentalLayoutApi::class)
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

    // Eyebrow → headline stat → cells, the same rhythm as its neighbour in the grid row and as a
    // Stations board card.
    Column(
        modifier = modifier.fillMaxWidth().homeGlass().homeCardPadding(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        HomeEyebrow("This week")
        Text(
            text = if (widget.sessionCount == 1) "1 session"
            else "${widget.sessionCount} sessions",
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(spacing.sm))

        // The seven days wrap instead of sitting on one line: at half the screen width they no longer
        // fit in a row. FlowRow rather than a hardcoded chunk so the break point follows the available
        // width — four-and-three on a normal phone, three rows on a narrow one — instead of
        // overflowing the card on small screens.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.smd),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            week.forEach { cell -> DayDot(cell) }
        }
    }
}

/** Sized so four cells plus their gutters fit one row of a half-width card on a normal phone; on a
 *  narrower screen FlowRow drops to three and the block grows a row instead of overflowing. */
private val CellSize = 30.dp

/** Enough tint to read as "trained" at cell size without competing with today's solid fill. */
private val TrainedFillAlpha = 0.18f
private val TrainedStroke = 1.dp

/** The trained-day tick, straddling the top edge of its own cell. */
private val BadgeSize = 16.dp
private val BadgeOverhang = 4.dp

@Composable
private fun DayDot(cell: WeekCell) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
//        Text(
//            text = cell.letter,
//            style = MaterialTheme.typography.labelMedium,
//            color = colors.onSurfaceVariant,
//        )

        val base = Modifier.size(CellSize)
            .clip(MaterialTheme.shapes.small)

        // Trained days carry a tinted fill behind the stroke, not just the hairline: at this size an
        // outline alone reads as another empty cell, so the week strip rendered monochrome even in a
        // fully-trained week. Today stays solid — the two are still unambiguous.
        val boxModifier = when (cell.state) {
            DayState.TODAY -> base.background(colors.primary)
            DayState.TRAINED -> base
                .background(colors.primary.copy(alpha = TrainedFillAlpha))
                .border(TrainedStroke, colors.primary, MaterialTheme.shapes.small)

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
                        .offset(x = BadgeOverhang, y = -BadgeOverhang)
                        .size(BadgeSize)
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
