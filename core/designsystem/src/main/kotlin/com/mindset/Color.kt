package com.mindset

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/*
 * "Obsidian Performance" color tokens — the source of truth is docs/design-v2.md.
 * The single dark-only Material 3 color scheme, applied by [MindSetTheme].
 *
 * Reconciliation note (deliberate deviation from the raw design-v2.md dump): that dump is a
 * standard M3 dark export, so its `primary` is the algorithmic light TINT (#ffb4aa, pale salmon)
 * and the saturated electric red (#ff5545) sits in `primary-container`. But every button in the
 * MindSet Figma is solid electric red with WHITE text, and Material's Button fills with
 * colorScheme.primary/onPrimary. So we map electric red onto `primary` (+ white `onPrimary`) so
 * existing components render the Figma look with zero edits, and keep the pale salmon available as
 * [ObsidianCoral] for the coral accents (Profile PRO tag, metric tints).
 */

// Primary — electric red as the high-emphasis action color (overrides design-v2's pale tonal value).
private val Primary = Color(0xFFE5484D)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFFF3B30)
private val OnPrimaryContainer = Color(0xFFFFFFFF)
private val InversePrimary = Color(0xFFC0000A)

/** The pale-salmon accent from design-v2.md (`#ffb4aa`), used for coral tints / PRO tags. */
val ObsidianCoral = Color(0xFFFFB4AA)

// Secondary — neutral grey for supporting emphasis (design-v2.md).
private val Secondary = Color(0xFFC6C6C7)
private val OnSecondary = Color(0xFF2F3131)
private val SecondaryContainer = Color(0xFF454747)
private val OnSecondaryContainer = Color(0xFFB4B5B5)

// Tertiary — neutral grey (design-v2.md).
private val Tertiary = Color(0xFFC8C6C8)
private val OnTertiary = Color(0xFF303032)
private val TertiaryContainer = Color(0xFF929092)
private val OnTertiaryContainer = Color(0xFF2A292C)

// Error.
private val ErrorColor = Color(0xFFFFB4AB)
private val OnError = Color(0xFF690005)
private val ErrorContainer = Color(0xFF93000A)
private val OnErrorContainer = Color(0xFFFFDAD6)

private val BackgroundColor = Color(0xFF131313)
private val OnBackground = Color(0xFFE5E2E1)
private val SurfaceColor = Color(0xFF131313)
private val OnSurfaceColor = Color(0xFFE5E2E1)
private val SurfaceVariant = Color(0xFF353534)
private val OnSurfaceVariant = Color(0xFFE7BDB7)
private val SurfaceTint = Color(0xFFFFB4AA)
private val SurfaceDim = Color(0xFF131313)
private val SurfaceBright = Color(0xFF3A3939)
private val SurfaceContainerLowest = Color(0xFF0E0E0E)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val SurfaceContainerHighest = Color(0xFF353534)

// Inverse (for snackbars, etc.).
private val InverseSurface = Color(0xFFE5E2E1)
private val InverseOnSurface = Color(0xFF313030)

// Outlines — warm strokes against the obsidian surface (M3 dark: strokes, not shadows).
private val OutlineColor = Color(0xFFAD8883)
private val OutlineVariant = Color(0xFF5D3F3B)
val GlassFill = Color.White.copy(alpha = 0.05f)
val GlassBorder = Color.White.copy(alpha = 0.12f)
val TrendImprovingColor = Color(0xFF7CD672)
val FloatingTagText = Color(0xFFCBEF97)

/** The Obsidian Performance (dark) color scheme, selectable in [MindSetTheme]. */
internal val MindSetColorScheme =
    darkColorScheme(
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
