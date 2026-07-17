package dev.cadence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val PICKED_EXERCISE = "pickedExercise"

private object Routes {
    const val NEW_SESSION = "newSession"
    const val LOG_WORKOUT = "logWorkout/{sessionId}"
    const val EXERCISE_PICKER = "exercisePicker"
    const val EXERCISE_DETAIL = "exerciseDetail/{exerciseId}"
    const val SESSION_DETAIL = "sessionDetail/{sessionId}"
    fun logWorkout(sessionId: String) = "logWorkout/$sessionId"
    fun exerciseDetail(exerciseId: String) = "exerciseDetail/$exerciseId"
    fun sessionDetail(sessionId: String) = "sessionDetail/$sessionId"
}

/**
 * App navigation (JetBrains Compose Navigation, kept native per the PRD). The four tabs
 * (Home/History/Stats/Profile) are top-level destinations reached via [switchTab]; New Session,
 * Log Workout, Exercise Picker and Session Detail are full-screen pushes over them.
 */
@Composable
fun CadenceNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Tab.Home.route) {

        // ---- Bottom-nav tabs ----
        composable(Tab.Home.route) {
            HomeScreen(
                onOpenSession = { id -> nav.navigate(Routes.logWorkout(id)) },
                onNewSession = { nav.navigate(Routes.NEW_SESSION) },
                onOpenDetail = { id -> nav.navigate(Routes.sessionDetail(id)) },
                onSeeAll = { nav.switchTab(Tab.History.route) },
                onTab = nav::switchTab,
            )
        }
        composable(Tab.History.route) {
            HistoryScreen(
                onOpenDetail = { id -> nav.navigate(Routes.sessionDetail(id)) },
                onTab = nav::switchTab,
            )
        }
        composable(Tab.Stats.route) {
            StatsScreen(onTab = nav::switchTab)
        }
        composable(Tab.Profile.route) {
            ProfileScreen(onTab = nav::switchTab)
        }

        // ---- Full-screen pushes ----
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
                onFinish = { nav.popBackStack(Tab.Home.route, inclusive = false) },
            )
        }
        composable(Routes.EXERCISE_PICKER) {
            ExercisePickerScreen(
                onOpenDetail = { exerciseId -> nav.navigate(Routes.exerciseDetail(exerciseId)) },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = Routes.EXERCISE_DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType }),
        ) { entry ->
            val exerciseId = entry.arguments?.getString("exerciseId").orEmpty()
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onAdd = {
                    // Hand the pick to the Log Workout entry (2 back) and return to it.
                    nav.getBackStackEntry(Routes.LOG_WORKOUT).savedStateHandle[PICKED_EXERCISE] = exerciseId
                    nav.popBackStack(Routes.LOG_WORKOUT, inclusive = false)
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = Routes.SESSION_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { entry ->
            SessionDetailScreen(
                sessionId = entry.arguments?.getString("sessionId").orEmpty(),
                onBack = { nav.popBackStack() },
            )
        }
    }
}

/**
 * Switch to a bottom-nav tab. `popUpTo(startDestination) { saveState }` + `restoreState` +
 * `launchSingleTop` is the standard pattern so each tab keeps its own state and the back stack
 * doesn't grow a new entry every time you tap between tabs.
 */
private fun NavController.switchTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
