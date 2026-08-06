package com.mindset

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Kinetic Precision shape scale — from docs/design.md "Shapes".
 * Extra-small 4dp (text fields), small 8dp (chips), medium 12dp (cards),
 * large 16dp (nav drawers / large FABs), extra-large 28dp (top app bars, large dialogs).
 * Fully-rounded (pill) buttons/search bars use CircleShape directly at the call site.
 */
internal val KineticShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
