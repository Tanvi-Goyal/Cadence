package dev.cadence

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.cadence.domain.ThemeMode
import dev.cadence.domain.WeightUnit

/** The set of design tokens the screens paint with; swapped as a whole between light and dark. */
internal data class CadencePalette(
    val background: Color,
    val surface: Color,
    val surfaceHi: Color,
    val accent: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val danger: Color,
)

// Cadence dark palette (from the Figma design).
private val DarkPalette = CadencePalette(
    background = Color(0xFF0E0E0F),
    surface = Color(0xFF1A1A1C),
    surfaceHi = Color(0xFF232326),
    accent = Color(0xFFC7F04D),
    onAccent = Color(0xFF14210A),
    textPrimary = Color(0xFFF5F5F5),
    textSecondary = Color(0xFF8E8E93),
    danger = Color(0xFFE24B4A),
)

// Light counterpart — same lime accent, inverted neutrals.
private val LightPalette = CadencePalette(
    background = Color(0xFFF6F6F7),
    surface = Color(0xFFFFFFFF),
    surfaceHi = Color(0xFFECECEF),
    accent = Color(0xFF3C6B00),
    onAccent = Color(0xFFF5FFE0),
    textPrimary = Color(0xFF16160F),
    textSecondary = Color(0xFF6B6B70),
    danger = Color(0xFFC0322F),
)

/** Current palette, provided by [CadenceTheme]. */
internal val LocalPalette = staticCompositionLocalOf { DarkPalette }

/** Theme + unit preferences, provided ONCE at the app root (see [MainActivity]). */
internal val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
internal val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.KG }

// Color tokens read from the active palette, so `color = TextPrimary` re-resolves when the theme
// changes. All call sites are inside composables, so the @ReadOnlyComposable getters are valid.
internal val Background: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.background
internal val Surface: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.surface
internal val SurfaceHi: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.surfaceHi
internal val Accent: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.accent
internal val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onAccent
internal val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.textPrimary
internal val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.textSecondary
internal val Danger: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.danger

/**
 * Applies the palette + Material color scheme for the current [LocalThemeMode]. Screens still wrap
 * themselves in `CadenceTheme { }`; the mode comes from the root-provided [LocalThemeMode], so the
 * whole app re-themes from one preference (and previews just get the default).
 */
@Composable
internal fun CadenceTheme(content: @Composable () -> Unit) {
    val dark = when (LocalThemeMode.current) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = if (dark) DarkPalette else LightPalette
    val colors = if (dark) {
        darkColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent,
            background = palette.background, surface = palette.surface,
            onBackground = palette.textPrimary, onSurface = palette.textPrimary,
        )
    } else {
        lightColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent,
            background = palette.background, surface = palette.surface,
            onBackground = palette.textPrimary, onSurface = palette.textPrimary,
        )
    }
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
