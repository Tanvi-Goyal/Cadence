package com.mindset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mindset.icons.Apple
import com.mindset.icons.ChevronRight
import com.mindset.icons.Run
import com.mindset.icons.Shield
import com.mindset.icons.Sync
import com.mindset.icons.Watch

/**
 * A provider we intend to support. Deliberately androidMain-local and static: none of these has a
 * data source yet, so there is nothing for `ProfileUiState` to carry. The seam for later is one
 * field — when Health Connect lands this gains a `connected` flag sourced from the ViewModel, and
 * only the icon mapping stays here.
 */
private data class IntegrationRow(val label: String, val detail: String, val icon: ImageVector)

private val PlaceholderIntegrations = listOf(
    IntegrationRow("Health Connect", "Calories and heart rate", MindSetIcons.Shield),
    IntegrationRow("Apple Health", "Workouts and heart rate", MindSetIcons.Apple),
    IntegrationRow("Garmin", "Watch activity import", MindSetIcons.Watch),
    IntegrationRow("Strava", "Run and ride sync", MindSetIcons.Run),
    IntegrationRow("intervals.icu", "Training load and splits", MindSetIcons.Sync),
)

/**
 * The integrations roadmap, collapsed by default. Every row is an honest placeholder — none of these
 * providers is wired yet, so none is tappable and none carries a toggle that would pretend to
 * remember a connection.
 *
 * Expansion is UI-local [rememberSaveable] rather than ViewModel state: it is a view affordance with
 * no domain meaning, it already survives configuration change and process death, and hoisting it
 * would rebuild the whole `ProfileUiState` on every chevron tap.
 */
@Composable
fun IntegrationsCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    val shape = MaterialTheme.shapes.medium
    var expanded by rememberSaveable { mutableStateOf(false) }
    // `rotate` is a graphicsLayer wrapper, so the chevron animates in the draw phase — no re-measure.
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, label = "chevron")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Integrations".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${PlaceholderIntegrations.size} planned",
                style = MaterialTheme.typography.labelSmall,
                color = colors.outline,
            )
            Icon(
                MindSetIcons.ChevronRight,
                contentDescription = if (expanded) "Collapse integrations" else "Expand integrations",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp).rotate(rotation),
            )
        }

        // AnimatedVisibility, not animateContentSize: the latter keeps all five rows composed and
        // measured while collapsed, and cannot cross-fade.
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(modifier = Modifier.padding(bottom = spacing.sm)) {
                PlaceholderIntegrations.forEach { IntegrationPlaceholderRow(it) }
            }
        }
    }
}

@Composable
private fun IntegrationPlaceholderRow(row: IntegrationRow) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing

    Row(
        // No `clickable` at all — not even `clickable(enabled = false)`, which still installs a
        // semantics node so TalkBack announces a disabled button ("broken", not "not built yet").
        // No chevron either: that is this app's "this navigates" signal.
        modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.md, vertical = spacing.smd),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            row.icon,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            // Tinted deliberately rather than dimmed with alpha, which would wreck text contrast.
            Text(row.label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            Text(
                row.detail,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        SoonTag()
    }
}

@Composable
private fun SoonTag() {
    val shape = MaterialTheme.shapes.small
    Text(
        text = "Soon",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape)
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    )
}
