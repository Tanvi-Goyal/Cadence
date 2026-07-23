package dev.cadence

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** The Stats tab (string-routed). */
fun NavGraphBuilder.statsScreen(onTab: (Tab) -> Unit) {
    composable<Stats> {
        StatsScreen(onTab = onTab)
    }
}
