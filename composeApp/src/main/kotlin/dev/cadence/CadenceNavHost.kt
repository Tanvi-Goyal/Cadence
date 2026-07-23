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
        composable(Tab.Home.route) {
            HomeScreen(
                onOpenSession = { id -> nav.navigate(LogWorkout(id)) },
                onNewSession = { nav.navigate(NewSession) },
                onOpenTemplates = { nav.navigate(Templates) },
                onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
                onSeeAll = { nav.switchTab(Tab.History.route) },
                onTab = nav::switchTab,
            )
        }
        composable(Tab.History.route) {
            HistoryScreen(
                onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
                onTab = nav::switchTab,
            )
        }
        composable(Tab.Stats.route) {
            StatsScreen(onTab = nav::switchTab)
        }
        composable(Tab.Profile.route) {
            ProfileScreen(onTab = nav::switchTab, onOpenCredits = { nav.navigate(Credits) })
        }
        composable<Credits> {
            CreditsScreen(onBack = { nav.popBackStack() })
        }

        // ---- Full-screen pushes (type-safe routes) ----
        composable<NewSession> {
            NewSessionScreen(
                onBack = { nav.popBackStack() },
                onCreated = { id ->
                    // Replace New Session with Log Workout so Back returns Home, not the picker.
                    nav.navigate(LogWorkout(id)) {
                        popUpTo<NewSession> { inclusive = true }
                    }
                },
            )
        }
        composable<LogWorkout> { entry ->
            val sessionId = entry.toRoute<LogWorkout>().sessionId
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
                onAddExercise = { nav.navigate(ExercisePicker(PickerTarget.LOG)) },
                onFinish = { nav.popBackStack(Tab.Home.route, inclusive = false) },
            )
        }
        composable<ExercisePicker> { entry ->
            val target = entry.toRoute<ExercisePicker>().target
            ExercisePickerScreen(
                onOpenDetail = { exerciseId -> nav.navigate(ExerciseDetail(exerciseId, target)) },
                onBack = { nav.popBackStack() },
            )
        }
        composable<ExerciseDetail> { entry ->
            val route = entry.toRoute<ExerciseDetail>()
            ExerciseDetailScreen(
                exerciseId = route.exerciseId,
                onAdd = {
                    // Hand the pick back to whichever screen launched the picker (Log Workout or the
                    // Template Builder, 2 back) and return to it. getBackStackEntry<T>() matches the
                    // single entry of that route type on the stack, regardless of its arg value.
                    val origin = when (route.target) {
                        PickerTarget.LOG -> nav.getBackStackEntry<LogWorkout>()
                        PickerTarget.BUILDER -> nav.getBackStackEntry<TemplateBuilder>()
                    }
                    origin.savedStateHandle[PICKED_EXERCISE] = route.exerciseId
                    when (route.target) {
                        PickerTarget.LOG -> nav.popBackStack<LogWorkout>(inclusive = false)
                        PickerTarget.BUILDER -> nav.popBackStack<TemplateBuilder>(inclusive = false)
                    }
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable<SessionDetail> { entry ->
            SessionDetailScreen(
                sessionId = entry.toRoute<SessionDetail>().sessionId,
                onBack = { nav.popBackStack() },
            )
        }

        // ---- Templates (D2) ----
        composable<Templates> {
            TemplatesScreen(
                onBack = { nav.popBackStack() },
                onNewTemplate = { nav.navigate(NewTemplate) },
                onEditTemplate = { id -> nav.navigate(TemplateBuilder(id)) },
                onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
            )
        }
        composable<NewTemplate> {
            NewTemplateScreen(
                onBack = { nav.popBackStack() },
                onCreated = { id ->
                    // Replace New Template with the builder so Back returns to the Templates list.
                    nav.navigate(TemplateBuilder(id)) {
                        popUpTo<NewTemplate> { inclusive = true }
                    }
                },
            )
        }
        composable<TemplateBuilder> { entry ->
            val templateId = entry.toRoute<TemplateBuilder>().templateId
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
                onAddExercise = { nav.navigate(ExercisePicker(PickerTarget.BUILDER)) },
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
