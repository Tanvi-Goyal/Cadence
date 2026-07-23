package dev.cadence

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** The Stats tab (string-routed). */
fun NavGraphBuilder.statsScreen(onTab: (String) -> Unit) {
    composable(Tab.Stats.route) {
        StatsScreen(onTab = onTab)
    }
}
