package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/*
 * Home's own small building blocks. These replace the deprecated `MindSetUi.kt` primitives
 * (SectionLabel / IconMedallion / EmptyHint) that Home was the last big consumer of — the feature owns
 * its pieces rather than reaching into :core:designsystem for shared ones. Tokens only.
 */

/** Home's alias for the shared [glassSurface] card treatment (now in :core:ui, so History renders
 *  the identical card). Kept as a name Home's call sites already use. */
@Composable
internal fun Modifier.homeGlass(shape: Shape = MaterialTheme.shapes.medium, border: Color = GlassBorder): Modifier =
    glassSurface(shape, border)

/** @see liveAccentBorder */
@Composable
internal fun liveBorder(): Color = liveAccentBorder()

@Composable
internal fun HomeMedallion(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .size(MedallionSize)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(MedallionIconSize))
    }
}

private val MedallionSize = 40.dp
private val MedallionIconSize = 20.dp
