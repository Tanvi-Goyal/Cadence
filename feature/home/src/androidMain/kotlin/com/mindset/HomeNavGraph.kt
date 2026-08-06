package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** The Home tab (string-routed). All navigation targets are supplied by the app as lambdas. */
fun NavGraphBuilder.homeScreen(
    onOpenSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenTemplate: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    onTab: (Tab) -> Unit,
) {
    composable<Home> {
        HomeScreen(
            onOpenSession = onOpenSession,
            onNewSession = onNewSession,
            onOpenTemplates = onOpenTemplates,
            onOpenTemplate = onOpenTemplate,
            onOpenDetail = onOpenDetail,
            onSeeAll = onSeeAll,
            onTab = onTab,
        )
    }
}
