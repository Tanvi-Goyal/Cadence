package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.MindSetTopBar
import com.mindset.model.BottomNavTab
import com.mindset.presentation.LogTabViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Log tab: the capture surface wearing the app's standard tab chrome.
 *
 * All this adds over [LogWorkoutScreen] is *which* session to edit — [LogTabViewModel] resumes the
 * open draft (or creates one), because a tab has no route argument to carry a session id.
 */
@Composable
fun LogTabScreen(
    onAddExercise: () -> Unit,
    onTab: (BottomNavTab) -> Unit,
    onCompleted: () -> Unit,
    viewModel: LogTabViewModel = koinViewModel(),
) {
    val sessionId by viewModel.sessionId.collectAsStateWithLifecycle()

    val id = sessionId
    if (id == null) {
        // At most one frame, while the draft is read from the DB. The chrome is drawn anyway so the
        // tab doesn't flash a bare screen where the bars belong.
        ResolvingPlaceholder(onTab)
        return
    }

    LogWorkoutScreen(
        sessionId = id,
        // A tab root has no back destination: passing [onTab] suppresses the ✕ and the
        // discard-on-exit, so this is never invoked.
        onBack = {},
        onAddExercise = onAddExercise,
        onFinish = {
            viewModel.onCompleted()
            onCompleted()
        },
        onTab = onTab,
    )
}

@Composable
private fun ResolvingPlaceholder(onTab: (BottomNavTab) -> Unit) {
    MindSetTheme {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { MindSetTopBar(onProfileClick = { onTab(BottomNavTab.Profile) }) },
            bottomBar = { BottomNavBar(current = BottomNavTab.Log, onTabClick = onTab) },
        ) { inset ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(inset)
                    .background(MaterialTheme.colorScheme.background),
            )
        }
    }
}
