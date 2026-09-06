package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The app's in-content section header: an accent tick + an uppercase label, with an optional
 * [trailing] affordance ("See all", a filter button) pinned to the end.
 *
 * Promoted here from `:feature:home` so every list surface (Home, History) uses the one header
 * instead of a per-feature copy.
 */
@Composable
fun MindSetSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Box(
                Modifier
                    .width(AccentTickWidth)
                    .height(AccentTickHeight)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(colors.primary),
            )
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = colors.onSurface,
            )
        }
        trailing?.invoke()
    }
}

private val AccentTickWidth = 3.dp
private val AccentTickHeight = 14.dp
