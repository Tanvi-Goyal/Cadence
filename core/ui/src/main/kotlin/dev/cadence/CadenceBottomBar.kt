package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.cadence.icons.Add
import dev.cadence.icons.Grid
import dev.cadence.icons.NavAccount
import dev.cadence.icons.NavHistory
import dev.cadence.icons.NavHome

/** The four top-level tabs — a pure UI model (label + icon). The app maps each to its typed route. */
enum class Tab(val label: String) {
    Home("Home"),
    History("History"),
    Stations("Stations"),
    Profile("Profile"),
}

private fun Tab.icon(): ImageVector = when (this) {
    Tab.Home -> NavHome
    Tab.History -> NavHistory
    Tab.Stations -> CadenceIcons.Grid
    Tab.Profile -> NavAccount
}

/**
 * Ambient "quick start a workout" action for the center (+) FAB, provided once by the nav host (it
 * needs the NavController). Kept as a CompositionLocal so the FAB works on every tab without threading
 * a callback through each screen's signature.
 */
val LocalQuickStart = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Shared bottom nav, rendered by each tab screen. Layout: Home · History · [center Quick-Start FAB] ·
 * Stations · Profile. The active tab is a filled primary-container pill; inactive tabs are muted
 * icon + label. [current] highlights the active tab; the FAB fires [LocalQuickStart].
 */
@Composable
fun CadenceBottomBar(
    current: Tab,
    onTab: (Tab) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val quickStart = LocalQuickStart.current
    Column(Modifier.background(colors.surfaceContainerLow)) {
        HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow)
                .padding(
                    horizontal = MaterialTheme.spacing.md,
                    vertical = MaterialTheme.spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(Tab.Home, active = current == Tab.Home, onClick = { onTab(Tab.Home) })
            NavItem(Tab.History, active = current == Tab.History, onClick = { onTab(Tab.History) })
            QuickStartFab(onClick = quickStart)
            NavItem(Tab.Stations, active = current == Tab.Stations, onClick = { onTab(Tab.Stations) })
            NavItem(Tab.Profile, active = current == Tab.Profile, onClick = { onTab(Tab.Profile) })
        }
    }
}

@Composable
private fun QuickStartFab(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Slightly raised solid-primary circle with a white +, matching the Figma center action.
    Box(
        modifier = Modifier
            .offset(y = (-8).dp)
            .size(56.dp)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CadenceIcons.Add,
            contentDescription = "Quick start",
            tint = colors.onPrimary,
            modifier = Modifier.size(28.dp),
        )
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
            .clickable(enabled = !active, onClick = onClick)
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
