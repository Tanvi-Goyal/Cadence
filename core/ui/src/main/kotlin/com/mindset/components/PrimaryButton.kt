package com.mindset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindset.spacing

/**
 * The app's high-emphasis action. [leadingIcon] is for the few actions whose glyph carries meaning on
 * its own — Start Workout's play triangle — so those screens use this button rather than a look-alike.
 */
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    val bg = if (enabled) colors.primary else colors.surfaceContainerHigh
    val fg = if (enabled) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.5f)
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.smd, horizontal = MaterialTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(LeadingIconSize),
            )
        }
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

private val LeadingIconSize = 14.dp
