package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindset.presentation.WeekFrequencyUi

/** The plot area. A visual box dimension, not a layout gap — a raw dp is correct here. */
private val BarAreaHeight = 120.dp

/** A zero-session week still draws a sliver, so the axis reads as N weeks rather than going gappy. */
private const val MinBarFraction = 0.05f

/**
 * Sessions per ISO week over the recent past — one bar per week, oldest left, the in-progress week
 * accented.
 *
 * Hand-rolled from layout primitives rather than a Canvas: these marks are axis-aligned rounded
 * rects, which `clip` + `background` already draw, so a Canvas would forfeit theming, RTL and
 * semantics and buy nothing. (`SheenProgress` uses Canvas because it needs a curved `Path`; there is
 * no primitive for that.)
 *
 * Recomposition: [weeks] is an unstable `List`, so under strong skipping it is compared by instance.
 * The ViewModel's `distinctUntilChanged` is what keeps the instance stable across unrelated session
 * edits — without it this card re-lays-out every bar on any write to the session table.
 */
@Composable
fun TrainingFrequencyCard(weeks: List<WeekFrequencyUi>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    val shape = MaterialTheme.shapes.medium
    // Kept off the recomposition path: recomputed only when the bucket list itself changes.
    val maxCount = remember(weeks) { weeks.maxOfOrNull { it.sessionCount }?.coerceAtLeast(1) ?: 1 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Training frequency".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            // In the header rather than over the last bar, where it would fight the bar heights.
            Text(
                text = "Active",
                style = MaterialTheme.typography.labelSmall,
                color = ObsidianCoral,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(BarAreaHeight),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.Bottom,
        ) {
            weeks.forEach { week ->
                FrequencyBar(week = week, maxCount = maxCount, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FrequencyBar(week: WeekFrequencyUi, maxCount: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    val fraction = (week.sessionCount.toFloat() / maxCount).coerceIn(MinBarFraction, 1f)

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (week.isCurrent) ObsidianCoral else colors.primary.copy(alpha = 0.30f)),
            )
        }

        Text(
            text = week.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (week.isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (week.isCurrent) ObsidianCoral else colors.onSurfaceVariant,
        )
    }
}
