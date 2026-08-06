package com.mindset

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Kinetic Precision color tokens — the single source of truth is docs/design.md.
 * These are the raw hex values from that spec, mapped 1:1 onto the Material 3 role set.
 * The design system is dark-only (see docs/design.md "Material 3 Dark Theme"), so there is
 * exactly one ColorScheme.
 */

// Primary — vibrant, desaturated "Kinetic Green" for high-emphasis components.
private val Primary = Color(0xFFCFF39A)
private val OnPrimary = Color(0xFF213600)
private val PrimaryContainer = Color(0xFFB3D681)
private val OnPrimaryContainer = Color(0xFF415D17)
private val InversePrimary = Color(0xFF4A671F)

// Secondary — muted olive for supporting emphasis.
private val Secondary = Color(0xFFC2CAAF)
private val OnSecondary = Color(0xFF2C3320)
private val SecondaryContainer = Color(0xFF444C37)
private val OnSecondaryContainer = Color(0xFFB4BCA1)

// Tertiary — teal accent for status-neutral information (avoids primary-color fatigue).
private val Tertiary = Color(0xFFC0F0EB)
private val OnTertiary = Color(0xFF003734)
private val TertiaryContainer = Color(0xFFA4D4CF)
private val OnTertiaryContainer = Color(0xFF2E5D59)

// Error.
private val ErrorColor = Color(0xFFFFB4AB)
private val OnError = Color(0xFF690005)
private val ErrorContainer = Color(0xFF93000A)
private val OnErrorContainer = Color(0xFFFFDAD6)

// Surfaces — "Deep Obsidian" base with tonal container layers for elevation.
private val BackgroundColor = Color(0xFF12140E)
private val OnBackground = Color(0xFFE3E3D9)
private val SurfaceColor = Color(0xFF12140E)
private val OnSurfaceColor = Color(0xFFE3E3D9)
private val SurfaceVariant = Color(0xFF34352F)
private val OnSurfaceVariant = Color(0xFFC4C8B7)
private val SurfaceTint = Color(0xFFAFD27D)
private val SurfaceDim = Color(0xFF12140E)
private val SurfaceBright = Color(0xFF383A33)
private val SurfaceContainerLowest = Color(0xFF0D0F09)
private val SurfaceContainerLow = Color(0xFF1A1C16)
private val SurfaceContainer = Color(0xFF1E201A)
private val SurfaceContainerHigh = Color(0xFF292B24)
private val SurfaceContainerHighest = Color(0xFF34352F)

// Inverse (for snackbars, etc.).
private val InverseSurface = Color(0xFFE3E3D9)
private val InverseOnSurface = Color(0xFF2F312B)

// Outlines — define component boundaries against the obsidian surface (M3 dark: strokes, not shadows).
private val OutlineColor = Color(0xFF8E9383)
private val OutlineVariant = Color(0xFF44483B)

/** The canonical Kinetic Precision (dark) color scheme, wired in [MindSetTheme]. */
internal val KineticColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = ErrorColor,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundColor,
    onBackground = OnBackground,
    surface = SurfaceColor,
    onSurface = OnSurfaceColor,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = OutlineColor,
    outlineVariant = OutlineVariant,
)
