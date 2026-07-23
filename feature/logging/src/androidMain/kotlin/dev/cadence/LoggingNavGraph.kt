package dev.cadence

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

/** New Session (session-type picker → creates a session). */
fun NavGraphBuilder.newSessionScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
) {
    composable<NewSession> {
        NewSessionScreen(onBack = onBack, onCreated = onCreated)
    }
}

/**
 * Live workout logging. The exercise picker writes the chosen id into this entry's savedStateHandle
 * on the way back; we observe it and hand it to the screen (which adds it via its own VM).
 */
fun NavGraphBuilder.logWorkoutScreen(
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onFinish: () -> Unit,
) {
    composable<LogWorkout> { entry ->
        val sessionId = entry.toRoute<LogWorkout>().sessionId
        val picked by entry.savedStateHandle
            .getStateFlow<String?>(PICKED_EXERCISE, null)
            .collectAsStateWithLifecycle()
        LogWorkoutScreen(
            sessionId = sessionId,
            pickedExerciseId = picked,
            onExerciseConsumed = { entry.savedStateHandle[PICKED_EXERCISE] = null },
            onBack = onBack,
            onAddExercise = onAddExercise,
            onFinish = onFinish,
        )
    }
}
