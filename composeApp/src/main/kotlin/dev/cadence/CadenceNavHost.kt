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
    const val EXERCISE_PICKER = "exercisePicker/{target}"
    const val EXERCISE_DETAIL = "exerciseDetail/{exerciseId}/{target}"
    const val SESSION_DETAIL = "sessionDetail/{sessionId}"
    const val CREDITS = "credits"
    const val TEMPLATES = "templates"
    const val NEW_TEMPLATE = "newTemplate"
    const val TEMPLATE_BUILDER = "templateBuilder/{templateId}"
    fun logWorkout(sessionId: String) = "logWorkout/$sessionId"
    fun exercisePicker(target: String) = "exercisePicker/$target"
    fun exerciseDetail(exerciseId: String, target: String) = "exerciseDetail/$exerciseId/$target"
    fun sessionDetail(sessionId: String) = "sessionDetail/$sessionId"
    fun templateBuilder(templateId: String) = "templateBuilder/$templateId"
}

/**
 * Where the shared exercise picker should hand its pick back to. Both the Log Workout and the
 * Template Builder launch the same picker → detail flow; the target rides through as a route arg so
 * the detail screen knows which back-stack entry to return the chosen exercise to.
 */
private object PickerTarget {
    const val LOG = "log"
    const val BUILDER = "builder"
    fun routePattern(target: String) = if (target == BUILDER) Routes.TEMPLATE_BUILDER else Routes.LOG_WORKOUT
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
                onOpenTemplates = { nav.navigate(Routes.TEMPLATES) },
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
            ProfileScreen(onTab = nav::switchTab, onOpenCredits = { nav.navigate(Routes.CREDITS) })
        }
        composable(Routes.CREDITS) {
            CreditsScreen(onBack = { nav.popBackStack() })
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
                onAddExercise = { nav.navigate(Routes.exercisePicker(PickerTarget.LOG)) },
                onFinish = { nav.popBackStack(Tab.Home.route, inclusive = false) },
            )
        }
        composable(
            route = Routes.EXERCISE_PICKER,
            arguments = listOf(navArgument("target") { type = NavType.StringType }),
        ) { entry ->
            val target = entry.arguments?.getString("target") ?: PickerTarget.LOG
            ExercisePickerScreen(
                onOpenDetail = { exerciseId -> nav.navigate(Routes.exerciseDetail(exerciseId, target)) },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = Routes.EXERCISE_DETAIL,
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.StringType },
                navArgument("target") { type = NavType.StringType },
            ),
        ) { entry ->
            val exerciseId = entry.arguments?.getString("exerciseId").orEmpty()
            val target = entry.arguments?.getString("target") ?: PickerTarget.LOG
            val returnRoute = PickerTarget.routePattern(target)
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onAdd = {
                    // Hand the pick back to whichever screen launched the picker (Log Workout or the
                    // Template Builder, 2 back) and return to it.
                    nav.getBackStackEntry(returnRoute).savedStateHandle[PICKED_EXERCISE] = exerciseId
                    nav.popBackStack(returnRoute, inclusive = false)
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

        // ---- Templates (D2) ----
        composable(Routes.TEMPLATES) {
            TemplatesScreen(
                onBack = { nav.popBackStack() },
                onNewTemplate = { nav.navigate(Routes.NEW_TEMPLATE) },
                onEditTemplate = { id -> nav.navigate(Routes.templateBuilder(id)) },
                onStarted = { sessionId -> nav.navigate(Routes.logWorkout(sessionId)) },
            )
        }
        composable(Routes.NEW_TEMPLATE) {
            NewTemplateScreen(
                onBack = { nav.popBackStack() },
                onCreated = { id ->
                    // Replace New Template with the builder so Back returns to the Templates list.
                    nav.navigate(Routes.templateBuilder(id)) {
                        popUpTo(Routes.NEW_TEMPLATE) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.TEMPLATE_BUILDER,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType }),
        ) { entry ->
            val templateId = entry.arguments?.getString("templateId").orEmpty()
            // Same picker handoff as Log Workout: the picker writes the chosen exercise id into this
            // entry's savedStateHandle, and the builder's VM adds it.
            val picked by entry.savedStateHandle
                .getStateFlow<String?>(PICKED_EXERCISE, null)
                .collectAsStateWithLifecycle()
            TemplateBuilderScreen(
                templateId = templateId,
                pickedExerciseId = picked,
                onExerciseConsumed = { entry.savedStateHandle[PICKED_EXERCISE] = null },
                onBack = { nav.popBackStack() },
                onAddExercise = { nav.navigate(Routes.exercisePicker(PickerTarget.BUILDER)) },
                onDone = { nav.popBackStack() },
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
