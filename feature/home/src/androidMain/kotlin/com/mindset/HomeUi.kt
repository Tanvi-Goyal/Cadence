package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Home's own small building blocks. These replace the deprecated `MindSetUi.kt` primitives
 * (SectionLabel / IconMedallion / EmptyHint) that Home was the last big consumer of — the feature owns
 * its pieces rather than reaching into :core:designsystem for shared ones. Tokens only.
 */

/** The hairline "glass" surface every Home card sits on: translucent white fill + 1dp stroke.
 *  No real backdrop blur — that is a per-frame GPU cost; at 5% fill over the obsidian background the
 *  static approximation is indistinguishable at card size. Callers append `.clickable`/`.padding`
 *  after this so the ripple stays inside the clip.
 *
 *  [border] exists for the one card that earns a tinted edge — the live race (see [liveBorder]).
 *  Everything else takes the neutral default; a second accent-bordered card would stop the first from
 *  reading as "this one is happening right now". */
@Composable
internal fun Modifier.homeGlass(
    shape: Shape = MaterialTheme.shapes.medium,
    border: Color = GlassBorder,
): Modifier = clip(shape).background(GlassFill).border(GlassStroke, border, shape)

private val GlassStroke = 1.dp

/** The live-race edge: the brand red at half strength — present enough to mark the card as live,
 *  faint enough that it still reads as a hairline rather than a warning. */
@Composable
internal fun liveBorder(): Color = MaterialTheme.colorScheme.primary.copy(alpha = LiveBorderAlpha)

private const val LiveBorderAlpha = 0.5f

/** Content inset for a Home card, matching the Stations board's cards: tighter on the sides than the
 *  top and bottom, so a full-bleed row of cells inside a card still lines up with the screen gutter. */
@Composable
internal fun Modifier.homeCardPadding(): Modifier =
    padding(horizontal = MaterialTheme.spacing.smd, vertical = MaterialTheme.spacing.md)

/** The small caps line that opens a card — the Stations board's card-header treatment. Names what the
 *  numbers below it are, so a standalone card needs no section heading above it. */
@Composable
internal fun HomeEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

/** Section heading: a short accent tick, then the uppercase tracked label. The tick is the cheapest
 *  way to get the brand red onto an otherwise monochrome page — one 3×14dp rect per section. */
@Composable
internal fun HomeSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Box(
                Modifier
                    .width(AccentTickWidth)
                    .height(AccentTickHeight)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(colors.primary),
            )
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = SectionTracking,
                color = colors.onSurface,
            )
        }
        trailing?.invoke()
    }
}

private val AccentTickWidth = 3.dp
private val AccentTickHeight = 14.dp

/** Section labels are tracked out to read as functional labels rather than prose (design.md). */
private val SectionTracking = 0.7.sp

/** Circular icon medallion (leading element on a Home row). */
@Composable
internal fun HomeMedallion(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .size(MedallionSize)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(MedallionIconSize))
    }
}

private val MedallionSize = 40.dp
private val MedallionIconSize = 20.dp

/** Centered helper for a Home widget's empty state. */
@Composable
internal fun HomeEmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
