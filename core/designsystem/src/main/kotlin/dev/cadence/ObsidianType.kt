package dev.cadence

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.cadence.designsystem.R

/*
 * "Obsidian Performance" type scale — source of truth docs/design-v2.md. A second scale living
 * alongside Kinetic's (see Type.kt), selected in [CadenceTheme].
 *
 * design-v2.md pairs Inter (headlines heavy + tight tracking, body relaxed) with JetBrains Mono
 * for the `label-caps` role (metadata / timestamps / caps tags — the "instrument readout" feel).
 * Tracking is expressed in em in the spec, applied here as `.em` (font-size-relative), matching
 * the design values directly.
 *
 * Inter faces: Normal/Medium reuse the bundled Kinetic faces; SemiBold/Bold/ExtraBold are the
 * heavier static faces added for this theme. JetBrains Mono ships Regular + Medium only, so the
 * `600` weight design-v2 asks for on label-caps is rendered with the Medium face (closest bundled,
 * avoids synthetic emboldening).
 */

internal val ObsidianInterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

/** JetBrains Mono — the `label-caps` / metric-readout face (design-v2.md). */
val MonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)

internal val ObsidianTypography = Typography(
    // display-lg (48/800) + derived medium/small. Heavy weight + tight tracking = commanding.
    displayLarge = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-0.02).em,
    ),
    displayMedium = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.02).em,
    ),
    displaySmall = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.01).em,
    ),
    // headline-lg (32/700) + headline-lg-mobile (28/700) + small.
    headlineLarge = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.01).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp,
    ),
    // title-md (20/600) anchors titleMedium; large/small bracket it.
    titleLarge = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp,
    ),
    // body-lg (16/400) + body-sm (14/400) + small.
    bodyLarge = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
    ),
    // labelLarge stays Inter (button text: "NEXT CONFIGURATION" is Inter semibold caps).
    labelLarge = TextStyle(
        fontFamily = ObsidianInterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.06.em,
    ),
    // label-caps role → JetBrains Mono, wide 0.1em tracking (section labels, meta, tags).
    labelMedium = TextStyle(
        fontFamily = MonoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.1.em,
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.1.em,
    ),
)
