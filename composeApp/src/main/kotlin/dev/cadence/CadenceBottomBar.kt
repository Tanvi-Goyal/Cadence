package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The four top-level tabs. `route` doubles as the nav destination. */
internal enum class Tab(val route: String, val label: String) {
    Home("home", "Home"),
    History("history", "History"),
    Stats("stats", "Stats"),
    Profile("profile", "Profile"),
}

/** Shared bottom nav, rendered by each tab screen. [current] highlights the active tab. */
@Composable
internal fun CadenceBottomBar(current: String, onTab: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab.entries.forEach { tab ->
            val active = tab.route == current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { if (!active) onTab(tab.route) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) Accent else Surface),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    tab.label,
                    color = if (active) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
