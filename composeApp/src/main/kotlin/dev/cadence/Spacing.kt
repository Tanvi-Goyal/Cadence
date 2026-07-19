package dev.cadence

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Spacing tokens — Material 3 has no spacing scale, so Kinetic Precision defines its own on the
 * strict 8dp grid from docs/design.md (xs=4 is the only sub-8 increment, for tight in-component
 * grouping). Provided via [LocalSpacing] in [CadenceTheme] and read as `MaterialTheme.spacing.md`
 * so screens use tokens instead of raw dp literals.
 */
internal data class Spacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val xxxl: Dp = 64.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }

/** Convenience accessor: `MaterialTheme.spacing.md` inside any composable. */
internal val MaterialTheme.spacing: Spacing
    @Composable @ReadOnlyComposable get() = LocalSpacing.current
