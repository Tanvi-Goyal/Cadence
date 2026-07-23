package dev.cadence

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.cadence.domain.ThemeMode
import dev.cadence.domain.WeightUnit

/*
 * Theme entry point for the "Kinetic Precision" design system (docs/design.md).
 *
 * The design system is dark-only, so [CadenceTheme] always applies [KineticColorScheme],
 * [KineticTypography] and [KineticShapes], and provides the [Spacing] tokens. Screens still wrap
 * themselves in `CadenceTheme { }`; nesting is idempotent, so the root wrap in MainActivity and the
 * per-screen wraps both resolve to the same theme.
 */

/** Theme + unit preferences, provided ONCE at the app root (see [MainActivity]). */
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.KG }

/*
 * Legacy color accessors. Existing screens paint with these names; they now delegate to the M3
 * color roles so every screen adopts Kinetic Precision without edits. New/redesigned screens should
 * prefer MaterialTheme.colorScheme directly — these remain as a thin compatibility layer.
 */
val Background: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val Surface: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
val SurfaceHi: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
val Accent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val OnAccent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary
val TextPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
internal val Danger: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

/** Applies the Kinetic Precision M3 theme (color scheme + typography + shapes + spacing). */
@Composable
fun CadenceTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = KineticColorScheme,
            typography = KineticTypography,
            shapes = KineticShapes,
            content = content,
        )
    }
}
