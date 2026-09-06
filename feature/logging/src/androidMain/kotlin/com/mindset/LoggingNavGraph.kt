package com.mindset

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mindset.model.BottomNavTab

// fun NavGraphBuilder.newSessionScreen(
//    onBack: () -> Unit,
//    onCreated: (String) -> Unit,
// ) {
//    composable<NewSession> {
//        NewSessionScreen(onBack = onBack, onCreated = onCreated)
//    }
// }

/** The Log tab (no args — the tab resolves its own draft session). */
fun NavGraphBuilder.logTabScreen(onAddExercise: () -> Unit, onTab: (BottomNavTab) -> Unit, onCompleted: () -> Unit) {
    composable<Log> {
        LogTabScreen(onAddExercise = onAddExercise, onTab = onTab, onCompleted = onCompleted)
    }
}

/** Log Session as a push — started from a template or the quick-start FAB, with its own ✕. */
fun NavGraphBuilder.logWorkoutScreen(onBack: () -> Unit, onAddExercise: () -> Unit, onFinish: () -> Unit) {
    composable<LogWorkout> { entry ->
        val sessionId = entry.toRoute<LogWorkout>().sessionId
//        val picked by entry.savedStateHandle
//            .getStateFlow<String?>(PICKED_EXERCISE, null)
//            .collectAsStateWithLifecycle()
        LogWorkoutScreen(
            sessionId = sessionId,
            onBack = onBack,
            onAddExercise = onAddExercise,
            onFinish = onFinish,
        )
    }
}
