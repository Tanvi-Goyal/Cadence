package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mindset.model.BottomNavTab

/** The Stats tab (string-routed). */
fun NavGraphBuilder.statsScreen(onTab: (BottomNavTab) -> Unit) {
    composable<Stats> {
        StatsScreen(onTab = onTab)
    }
}
