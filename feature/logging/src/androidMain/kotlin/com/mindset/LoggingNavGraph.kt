package com.mindset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mindset.model.BottomNavTab

/** The Log tab (no args — the tab resolves its own draft session). */
fun NavGraphBuilder.logTabScreen(
    onTab: (BottomNavTab) -> Unit,
    onCompleted: () -> Unit,
) {
    composable<Log> {
        LogTabScreen(
            onAddExercise = { },
            onTab = onTab,
            onCompleted = onCompleted,
        )
    }
}

fun NavGraphBuilder.logWorkoutScreen(onBack: () -> Unit, onAddExercise: () -> Unit, onFinish: () -> Unit) {
    composable<LogWorkout> { entry ->
        val sessionId = entry.toRoute<LogWorkout>().sessionId
        LogWorkoutScreen(
            sessionId = sessionId,
            onBack = onBack,
            onAddExercise = onAddExercise,
            onFinish = onFinish,
        )
    }
}
