package com.mindset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.mindset.components.SessionRow
import com.mindset.presentation.Widget

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
        MindSetSectionHeader("Recent Sessions", Modifier.fillMaxWidth()) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = MaterialTheme.spacing.xs),
            )
        }
        if (widget.sessions.isEmpty()) {
            EmptyHomePlaceholder("No sessions yet. Start your first one.", Modifier.fillMaxWidth())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
            ) {
                widget.sessions.forEach { session ->
                    SessionRow(
                        session = session,
                        volumeKg = widget.volumesById[session.id] ?: 0.0,
                        durationSec = widget.durationsById[session.id],
                        onClick = { onOpenDetail(session.id) },
                        isPb = false,
                    )
                }
            }
        }
    }
}
