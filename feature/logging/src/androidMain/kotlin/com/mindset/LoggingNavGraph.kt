package com.mindset

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

// fun NavGraphBuilder.newSessionScreen(
//    onBack: () -> Unit,
//    onCreated: (String) -> Unit,
// ) {
//    composable<NewSession> {
//        NewSessionScreen(onBack = onBack, onCreated = onCreated)
//    }
// }

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
