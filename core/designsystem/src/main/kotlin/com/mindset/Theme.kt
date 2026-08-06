package com.mindset

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mindset.domain.ThemeMode
import com.mindset.domain.WeightUnit

/*
 * Theme entry point for the "Kinetic Precision" design system (docs/design.md).
 *
 * The design system is dark-only, so [MindSetTheme] always applies [KineticColorScheme],
 * [KineticTypography] and [KineticShapes], and provides the [Spacing] tokens. Screens still wrap
 * themselves in `MindSetTheme { }`; nesting is idempotent, so the root wrap in MainActivity and the
 * per-screen wraps both resolve to the same theme.
 */

/** Theme + unit preferences, provided ONCE at the app root (see [MainActivity]). */
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.KG }

/**
 * The two coexisting design systems. This is an ephemeral, in-memory comparison switch (the app
 * ships one of these long-term) — it is deliberately NOT persisted and defaults to [OBSIDIAN].
 * Kept in `:core:designsystem` (not a domain type) precisely because it never touches storage.
 */
enum class ThemeVariant { KINETIC, OBSIDIAN }

/** The active variant, provided at the app root; defaults to Obsidian and resets on process death. */
val LocalThemeVariant = staticCompositionLocalOf { ThemeVariant.OBSIDIAN }

/** Setter for [LocalThemeVariant], provided alongside it so any screen (e.g. Profile) can flip it. */
val LocalSetThemeVariant = staticCompositionLocalOf<(ThemeVariant) -> Unit> { {} }

/*
 * Legacy color accessors. Existing screens paint with these names; they now delegate to the M3
 * color roles so every screen adopts Kinetic Precision without edits. New/redesigned screens should
 * prefer MaterialTheme.colorScheme directly — these remain as a thin compatibility layer.
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

/**
 * Applies the active dark M3 theme (color scheme + typography + shapes + spacing). The variant is
 * read from [LocalThemeVariant] (provided at the app root), so screens keep wrapping in
 * `MindSetTheme { }` unchanged and both the root wrap and per-screen wraps resolve to the same
 * variant. Nesting stays idempotent.
 */
@Composable
fun MindSetTheme(content: @Composable () -> Unit) {
    val obsidian = LocalThemeVariant.current == ThemeVariant.OBSIDIAN
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = if (obsidian) ObsidianColorScheme else KineticColorScheme,
            typography = if (obsidian) ObsidianTypography else KineticTypography,
            shapes = if (obsidian) ObsidianShapes else KineticShapes,
            content = content,
        )
    }
}
