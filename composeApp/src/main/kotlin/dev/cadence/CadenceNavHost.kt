package dev.cadence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

/**
 * App navigation (JetBrains Compose Navigation, kept native per the PRD). The four tabs
 * (Home/History/Stats/Profile) are top-level destinations reached via [switchTab]; the arg-carrying
 * pushes (New Session, Log Workout, the shared Exercise Picker/Detail, Session Detail, Templates) are
 * type-safe `@Serializable` routes (see NavRoutes.kt) so args are compile-checked. The graph mixes the
 * string tab destinations with the typed pushes — navigation-compose supports both together.
 */
@Composable
fun CadenceNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Tab.Home.route) {

        // ---- Bottom-nav tabs (string-routed; no args) ----
        homeScreen(
            onOpenSession = { id -> nav.navigate(LogWorkout(id)) },
            onNewSession = { nav.navigate(NewSession) },
            onOpenTemplates = { nav.navigate(Templates) },
            onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
            onSeeAll = { nav.switchTab(Tab.History.route) },
            onTab = nav::switchTab,
        )
        historyScreen(
            onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
            onTab = nav::switchTab,
        )
        statsScreen(onTab = nav::switchTab)
        profileScreen(onTab = nav::switchTab, onOpenCredits = { nav.navigate(Credits) })
        creditsScreen(onBack = { nav.popBackStack() })

        // ---- Full-screen pushes (type-safe routes) ----
        newSessionScreen(
            onBack = { nav.popBackStack() },
            onCreated = { id ->
                // Replace New Session with Log Workout so Back returns Home, not the picker.
                nav.navigate(LogWorkout(id)) { popUpTo<NewSession> { inclusive = true } }
            },
        )
        logWorkoutScreen(
            onBack = { nav.popBackStack() },
            onAddExercise = { nav.navigate(ExercisePicker(PickerTarget.LOG)) },
            onFinish = { nav.popBackStack(Tab.Home.route, inclusive = false) },
        )
        exercisePickerScreen(
            onOpenDetail = { exerciseId, target -> nav.navigate(ExerciseDetail(exerciseId, target)) },
            onBack = { nav.popBackStack() },
        )
        exerciseDetailScreen(
            onAdd = { exerciseId, target ->
                // Hand the pick back to whichever screen launched the picker (Log Workout or the
                // Template Builder, 2 back) and return to it. getBackStackEntry<T>() matches the
                // single entry of that route type on the stack, regardless of its arg value.
                val origin = when (target) {
                    PickerTarget.LOG -> nav.getBackStackEntry<LogWorkout>()
                    PickerTarget.BUILDER -> nav.getBackStackEntry<TemplateBuilder>()
                }
                origin.savedStateHandle[PICKED_EXERCISE] = exerciseId
                when (target) {
                    PickerTarget.LOG -> nav.popBackStack<LogWorkout>(inclusive = false)
                    PickerTarget.BUILDER -> nav.popBackStack<TemplateBuilder>(inclusive = false)
                }
            },
            onBack = { nav.popBackStack() },
        )
        sessionDetailScreen(onBack = { nav.popBackStack() })

        // ---- Templates (D2) ----
        templatesScreen(
            onBack = { nav.popBackStack() },
            onNewTemplate = { nav.navigate(NewTemplate) },
            onEditTemplate = { id -> nav.navigate(TemplateBuilder(id)) },
            onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
        )
        newTemplateScreen(
            onBack = { nav.popBackStack() },
            onCreated = { id ->
                // Replace New Template with the builder so Back returns to the Templates list.
                nav.navigate(TemplateBuilder(id)) { popUpTo<NewTemplate> { inclusive = true } }
            },
        )
        templateBuilderScreen(
            onBack = { nav.popBackStack() },
            onAddExercise = { nav.navigate(ExercisePicker(PickerTarget.BUILDER)) },
            onDone = { nav.popBackStack() },
        )
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
