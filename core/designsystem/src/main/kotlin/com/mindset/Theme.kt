package com.mindset

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mindset.domain.ThemeMode
import com.mindset.domain.WeightUnit

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.KG }

/*
 * Legacy color accessors. Existing screens paint with these names; they delegate to the M3 color
 * roles so every screen adopts the theme without edits. New/redesigned screens should prefer
 * MaterialTheme.colorScheme directly — these remain as a thin compatibility layer.
 * // todo:: to be deleted after all screens are migrated to M3 color roles
 */
val Background: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val Surface: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
val SurfaceHi: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
val Accent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val OnAccent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary
val TextPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
internal val Danger: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

@Composable
fun MindSetTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = MindSetColorScheme,
            typography = ObsidianTypography,
            shapes = MindSetShapes,
            content = content,
        )
    }
}
