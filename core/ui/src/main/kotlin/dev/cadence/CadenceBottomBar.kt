package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The four top-level tabs. `route` doubles as the nav destination. */
enum class Tab(val route: String, val label: String) {
    Home("home", "Home"),
    History("history", "History"),
    Stats("stats", "Stats"),
    Profile("profile", "Profile"),
}

private fun Tab.icon(): ImageVector = when (this) {
    Tab.Home -> CadenceIcons.NavHome
    Tab.History -> CadenceIcons.NavHistory
    Tab.Stats -> CadenceIcons.NavStats
    Tab.Profile -> CadenceIcons.NavProfile
}

/**
 * Shared bottom nav, rendered by each tab screen. The active tab is a filled primary-container pill;
 * inactive tabs are muted icon + label. [current] highlights the active tab.
 */
@Composable
fun CadenceBottomBar(current: String, onTab: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.background(colors.surfaceContainerLow)) {
        HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .background(colors.surfaceContainerLow)
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { tab ->
                NavItem(
                    tab = tab,
                    active = tab.route == current,
                    onClick = { if (tab.route != current) onTab(tab.route) },
                )
            }
        }
    }
}

@Composable
private fun NavItem(tab: Tab, active: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) colors.primaryContainer else colors.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    ) {
        Icon(
            imageVector = tab.icon(),
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}
