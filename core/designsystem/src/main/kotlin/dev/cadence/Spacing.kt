package dev.cadence

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Spacing tokens — Material 3 has no spacing scale, so Kinetic Precision defines its own on the
 * 8dp baseline grid from docs/design.md. Two 4dp sub-grid steps are allowed for fine, in-component
 * work (per M3: 8dp for layout, 4dp increments for small elements): `xs`=4 for tight grouping and
 * `smd`=12 for component-internal padding (button/card insets). Provided via [LocalSpacing] in
 * [CadenceTheme] and read as `MaterialTheme.spacing.md` so screens use tokens instead of raw dp.
 */
data class Spacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val smd: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val xxxl: Dp = 64.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable @ReadOnlyComposable get() = LocalSpacing.current
