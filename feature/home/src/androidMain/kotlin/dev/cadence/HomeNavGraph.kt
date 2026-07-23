package dev.cadence

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** The Home tab (string-routed). All navigation targets are supplied by the app as lambdas. */
fun NavGraphBuilder.homeScreen(
    onOpenSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    onTab: (String) -> Unit,
) {
    composable(Tab.Home.route) {
        HomeScreen(
            onOpenSession = onOpenSession,
            onNewSession = onNewSession,
            onOpenTemplates = onOpenTemplates,
            onOpenDetail = onOpenDetail,
            onSeeAll = onSeeAll,
            onTab = onTab,
        )
    }
}
