package com.mindset

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mindset.presentation.SimulationArt
import com.mindset.presentation.SimulationEntry
import com.mindset.presentation.Widget
import com.mindset.ui.R

@Composable
fun SimulationCard(
    widget: Widget.SimulationWidget,
    onOpenTemplate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        MindSetSectionHeader("Simulations", Modifier.fillMaxWidth())

        LazyRow(
            modifier = Modifier.fillMaxWidth().height(RailHeight),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
            contentPadding = PaddingValues(end = MaterialTheme.spacing.xs),
        ) {
            items(widget.sims, key = { it.id }) { sim ->
                SimulationTile(sim, onClick = { onOpenTemplate(sim.id) })
            }
        }
    }
}

/** Card width is deliberately below the narrowest phone's content width so the next card peeks. */
private val TileWidth = 264.dp
private val RailHeight = 200.dp

@Composable
private fun SimulationTile(sim: SimulationEntry, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .width(TileWidth)
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(sim.art.drawableRes()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(scrimBrush()))
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md),
        ) {
            FlagBadge(sim.flag)
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
                sim.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.xs))
            Text(
                sim.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                sim.tags.forEach { FloatingTag(it) }
            }
        }
    }
}

/** Semantic art → the packaged photograph. Kept here so [SimulationEntry] stays free of Android types. */
private fun SimulationArt.drawableRes(): Int = when (this) {
    SimulationArt.FULL_HYROX -> R.drawable.template_full_hyrox
    SimulationArt.HALF_HYROX -> R.drawable.template_hyrox_sim
}

/** Bottom-anchored image scrim: transparent at the top → deepest surface at the bottom, so the
 *  bottom-anchored text keeps its contrast whatever the photograph does behind it. */
@Composable
private fun scrimBrush(): Brush {
    val deep = MaterialTheme.colorScheme.surfaceContainerLowest
    return Brush.verticalGradient(
        0.0f to Color.Transparent,
        0.5f to deep.copy(alpha = 0.4f),
        1.0f to deep,
    )
}

/** Accent flag naming the sim's format ("OFFICIAL SIM" / "HALF SIM"). */
@Composable
private fun FlagBadge(text: String) = Badge(
    text = text,
    shape = MaterialTheme.shapes.extraSmall,
    container = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    vertical = FlagVerticalPadding,
)

/** The rectangular flag sits tighter than [Spacing.xs]; 2dp is deliberate and has no token. */
private val FlagVerticalPadding = 2.dp

/** Translucent tag over the photo. No live backdrop-blur (a per-frame cost); the 0.8 alpha surface
 *  reads the same at this size. */
@Composable
private fun FloatingTag(text: String) = Badge(
    text = text,
    shape = CircleShape,
    container = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
    contentColor = FloatingTagText,
    vertical = MaterialTheme.spacing.xs,
)

/** One implementation behind both card tags — they differ only in shape, colours and how tight the
 *  vertical padding is, so the named wrappers above stay one-liners and can't drift apart. */
@Composable
private fun Badge(text: String, shape: Shape, container: Color, contentColor: Color, vertical: Dp) {
    Box(
        modifier = Modifier
            .clip(shape)
            .background(container)
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = vertical),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}
