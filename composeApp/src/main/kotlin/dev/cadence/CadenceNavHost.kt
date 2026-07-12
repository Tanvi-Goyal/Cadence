package dev.cadence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val PICKED_EXERCISE = "pickedExercise"

private object Routes {
    const val HOME = "home"
    const val NEW_SESSION = "newSession"
    const val LOG_WORKOUT = "logWorkout/{sessionId}"
    const val EXERCISE_PICKER = "exercisePicker"
    fun logWorkout(sessionId: String) = "logWorkout/$sessionId"
}

/**
 * App navigation. Native (JetBrains Compose Navigation), kept in the Android app per the PRD's
 * "navigation stays native" decision. Home → New Session → Log Workout, with the Exercise Picker
 * reached from Log Workout.
 */
@Composable
fun CadenceNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenSession = { id -> nav.navigate(Routes.logWorkout(id)) },
                onNewSession = { nav.navigate(Routes.NEW_SESSION) },
            )
        }
        composable(Routes.NEW_SESSION) {
            NewSessionScreen(
                onBack = { nav.popBackStack() },
                onCreated = { id ->
                    // Replace New Session with Log Workout so Back returns Home, not the picker.
                    nav.navigate(Routes.logWorkout(id)) {
                        popUpTo(Routes.NEW_SESSION) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.LOG_WORKOUT,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { entry ->
            val sessionId = entry.arguments?.getString("sessionId").orEmpty()
            // The picker writes the chosen exercise id into this entry's savedStateHandle on the
            // way back; observe it and hand it to the screen to add (in the screen's own VM).
            val picked by entry.savedStateHandle
                .getStateFlow<String?>(PICKED_EXERCISE, null)
                .collectAsStateWithLifecycle()
            LogWorkoutScreen(
                sessionId = sessionId,
                pickedExerciseId = picked,
                onExerciseConsumed = { entry.savedStateHandle[PICKED_EXERCISE] = null },
                onBack = { nav.popBackStack() },
                onAddExercise = { nav.navigate(Routes.EXERCISE_PICKER) },
                onFinish = { nav.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.EXERCISE_PICKER) {
            ExercisePickerScreen(
                onPick = { exerciseId ->
                    // Return the pick to the Log Workout entry, then pop back to it.
                    nav.previousBackStackEntry?.savedStateHandle?.set(PICKED_EXERCISE, exerciseId)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() },
            )
        }
    }
}
