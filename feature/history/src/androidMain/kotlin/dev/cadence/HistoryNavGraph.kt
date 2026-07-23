package dev.cadence

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

/** The History tab (string-routed). */
fun NavGraphBuilder.historyScreen(
    onOpenDetail: (String) -> Unit,
    onTab: (String) -> Unit,
) {
    composable(Tab.History.route) {
        HistoryScreen(onOpenDetail = onOpenDetail, onTab = onTab)
    }
}

/** The Session Detail push (typed route carries the sessionId). */
fun NavGraphBuilder.sessionDetailScreen(onBack: () -> Unit) {
    composable<SessionDetail> { entry ->
        SessionDetailScreen(sessionId = entry.toRoute<SessionDetail>().sessionId, onBack = onBack)
    }
}
