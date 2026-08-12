package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mindset.model.BottomNavTab

/** The Stations tab (string-routed) — the per-station Hyrox PB board. */
fun NavGraphBuilder.stationsScreen(onTab: (BottomNavTab) -> Unit) {
    composable<Stations> {
        StationsScreen(onTab = onTab)
    }
}
