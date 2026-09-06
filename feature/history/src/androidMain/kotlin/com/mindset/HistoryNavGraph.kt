package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

/** History — a push from Home's "See all", not a bottom-nav tab. */
fun NavGraphBuilder.historyScreen(onOpenDetail: (String) -> Unit, onBack: () -> Unit, onOpenProfile: () -> Unit) {
    composable<History> {
        HistoryScreen(onOpenDetail = onOpenDetail, onBack = onBack, onOpenProfile = onOpenProfile)
    }
}

/** The Session Detail push (typed route carries the sessionId). */
fun NavGraphBuilder.sessionDetailScreen(onBack: () -> Unit) {
    composable<SessionDetail> { entry ->
        SessionDetailScreen(sessionId = entry.toRoute<SessionDetail>().sessionId, onBack = onBack)
    }
}
