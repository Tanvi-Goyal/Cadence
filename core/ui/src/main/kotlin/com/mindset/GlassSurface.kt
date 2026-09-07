package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * The hairline "glass" surface every dashboard card sits on: translucent white fill + 1dp stroke.
 * No real backdrop blur — that is a per-frame GPU cost; at 5% fill over the obsidian background the
 * static approximation is indistinguishable at card size. Callers append `.clickable`/`.padding`
 * after this so the ripple stays inside the clip.
 *
 * Promoted here from `:feature:home` so History renders the same card as Home rather than a
 * look-alike that can drift.
 *
 * [border] exists for the one card that earns a tinted edge — the live race (see [liveAccentBorder]).
 * Everything else takes the neutral default; a second accent-bordered card would stop the first from
 * reading as "this one is happening right now".
 */
@Composable
fun Modifier.glassSurface(shape: Shape = MaterialTheme.shapes.medium, border: Color = GlassBorder): Modifier =
    clip(shape).background(GlassFill).border(GlassStroke, border, shape)

private val GlassStroke = 1.dp

/**
 * The live-race edge: the brand red at half strength — present enough to mark the card as live,
 * faint enough that it still reads as a hairline rather than a warning.
 */
@Composable
fun liveAccentBorder(): Color = MaterialTheme.colorScheme.primary.copy(alpha = LiveBorderAlpha)

private const val LiveBorderAlpha = 0.5f
