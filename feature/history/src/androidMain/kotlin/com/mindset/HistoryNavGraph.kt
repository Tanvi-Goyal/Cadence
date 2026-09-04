package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mindset.model.BottomNavTab

/** The History tab (string-routed). */
fun NavGraphBuilder.historyScreen(
    onOpenDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onTab: (BottomNavTab) -> Unit,
) {
    composable<History> {
        HistoryScreen(onOpenDetail = onOpenDetail, onTab = onTab)
    }
}

/** The Session Detail push (typed route carries the sessionId). */
fun NavGraphBuilder.sessionDetailScreen(onBack: () -> Unit) {
    composable<SessionDetail> { entry ->
        SessionDetailScreen(sessionId = entry.toRoute<SessionDetail>().sessionId, onBack = onBack)
    }
}
