package com.mindset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * The round header affordance a pushed screen carries instead of a toolbar — back on History detail,
 * back on the HYROX sim detail. Promoted out of `:feature:history` so the two screens can't drift into
 * two different back buttons.
 *
 * [container] exists for the one case that needs it: over the sim detail's photograph, where an opaque
 * circle punches a hole in the image and a translucent one does not.
 */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    Box(
        modifier = modifier
            .size(ButtonSize)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(IconSize),
        )
    }
}

private val ButtonSize = 40.dp
private val IconSize = 18.dp
