package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mindset.model.BottomNavTab

/** The Home tab (string-routed). All navigation targets are supplied by the app as lambdas. */
fun NavGraphBuilder.homeScreen(
    onOpenTemplate: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onSeeAll: () -> Unit,
    onTab: (BottomNavTab) -> Unit,
) {
    composable<Home> {
        HomeScreen(
            onOpenTemplate = onOpenTemplate,
            onOpenDetail = onOpenDetail,
            onSeeAll = onSeeAll,
            onTab = onTab,
        )
    }
}
