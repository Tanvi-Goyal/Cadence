package dev.cadence.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.cadence.GlassBorder
import dev.cadence.GlassFill
import dev.cadence.spacing

@Composable
fun PrimaryButton(
    text: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val bg = if (enabled) colors.primary else colors.surfaceContainerHigh
    val fg = if (enabled) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.5f)
    Box(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.smd, horizontal = MaterialTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}