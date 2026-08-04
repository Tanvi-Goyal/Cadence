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

val LocalQuickStart = staticCompositionLocalOf { {} }

@Composable
fun CadenceBottomBar(
    current: Tab,
    onTab: (Tab) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val quickStart = LocalQuickStart.current
    Column(Modifier.background(colors.surfaceContainerLow)) {
        HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
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
            NavItem(
                Tab.Stations,
                active = current == Tab.Stations,
                onClick = { onTab(Tab.Stations) })
            NavItem(Tab.Profile, active = current == Tab.Profile, onClick = { onTab(Tab.Profile) })
        }
    }
}

@Composable
private fun QuickStartFab(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .offset(y = (-32).dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CadenceIcons.Add,
            contentDescription = "Quick start",
            tint = colors.onPrimary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun NavItem(tab: Tab, active: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (active) colors.primaryContainer else colors.onSecondaryContainer
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs / 2),
        modifier = Modifier
            .clip(CircleShape)
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
            color = contentColor,
        )
    }
}
