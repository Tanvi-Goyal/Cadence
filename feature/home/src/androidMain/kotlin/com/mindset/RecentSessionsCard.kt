package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mindset.presentation.Widget

/**
 * The "Recent" widget: a grouped card of the latest sessions. Reuses the shared [SessionRow], now fed
 * real strength volume from [Widget.RecentSessionsWidget.volumesById] (rows fall back to their duration
 * when a session has no volume). The PB tag stays off for now (a session-level PB needs a whole-history
 * scan — see the plan's decision note).
 */
@Composable
fun RecentSessionsCard(
    widget: Widget.RecentSessionsWidget,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("Recent")
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = MaterialTheme.spacing.xs, vertical = 2.dp),
            )
        }
        if (widget.sessions.isEmpty()) {
            EmptyHint("No sessions yet. Start your first one.", Modifier.fillMaxWidth())
        } else {
            // Grouped card: rows sit on the container surface, separated by 1dp gaps that reveal the
            // fainter backing color (matches the Figma "Overlay" grouping).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.outlineVariant.copy(alpha = 0.2f)),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                widget.sessions.take(4).forEach { session ->
                    SessionRow(
                        session = session,
                        volumeKg = widget.volumesById[session.id] ?: 0.0,
                        onClick = { onOpenDetail(session.id) },
                        isPb = false,
                    )
                }
            }
        }
    }
}
