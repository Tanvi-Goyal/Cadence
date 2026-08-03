package dev.cadence

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * "Obsidian Performance" shape scale — from docs/design-v2.md "rounded" tokens (0.25/0.5/0.75/1/1.5rem).
 * A second scale living alongside Kinetic's (see Shape.kt), selected in [CadenceTheme].
 * Extra-small 4dp (sm), small 8dp (DEFAULT), medium 12dp (md), large 16dp (lg), extra-large 24dp (xl).
 * Fully-rounded (pill) tags/status chips use CircleShape directly at the call site.
 */
internal val ObsidianShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
