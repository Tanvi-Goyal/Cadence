package dev.cadence

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

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
    NavHost(
        navController = nav,
        startDestination = Login
    ) {

        // ---- Auth (app entry; design-static — any sign-in action enters the app) ----
        loginScreen(
            onSignedIn = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
            onCreateAccount = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
            onForgotPassword = {},
        )

        // ---- Bottom-nav tabs (typed routes; no args) ----
        homeScreen(
            onOpenSession = { id -> nav.navigate(LogWorkout(id)) },
            onNewSession = { nav.navigate(NewSession) },
            onOpenTemplates = { nav.navigate(Templates) },
            onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
            onSeeAll = { nav.switchTab(Tab.History) },
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
            onFinish = { nav.popBackStack(Home, inclusive = false) },
        )
        exercisePickerScreen(
            onOpenDetail = { exerciseId, target ->
                nav.navigate(
                    ExerciseDetail(
                        exerciseId,
                        target
                    )
                )
            },
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
            // A Library card now opens the read-only Template Detail (not the builder). The Hyrox sims
            // use the station-list variant; the strength blocks use the exercise-list variant; everything
            // else uses the workout-protocol variant. This id branch is a temporary shim until a real
            // template "kind" drives the layout choice.
            onEditTemplate = { id ->
                when (id) {
                    "full-hyrox-simulation", "half-hyrox-sim" -> nav.navigate(TemplateHyroxDetail(id))
                    "upper-strength-a", "upper-strength-b", "lower-body" -> nav.navigate(TemplateStrengthDetail(id))
                    else -> nav.navigate(TemplateDetail(id))
                }
            },
            onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
        )
        templateDetailScreen(
            onBack = { nav.popBackStack() },
            // Placeholder until the data pass: begin a fresh workout. Real behaviour = instantiate this
            // template (deep copy) → open Log Workout.
            onStart = { nav.navigate(NewSession) },
            onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
        )
        templateHyroxDetailScreen(
            onBack = { nav.popBackStack() },
            onStart = { nav.navigate(NewSession) },
            onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
        )
        templateStrengthDetailScreen(
            onBack = { nav.popBackStack() },
            onStart = { nav.navigate(NewSession) },
            onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
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
 * Switch to a bottom-nav tab. Maps the UI [Tab] to its typed route, then applies the standard
 * multi-back-stack pattern (`popUpTo(startDestination){saveState}` + `restoreState` +
 * `launchSingleTop`) so each tab keeps its own back stack and tapping between tabs doesn't grow one.
 */
private fun NavController.switchTab(tab: Tab) {
    val route: Any = when (tab) {
        Tab.Home -> Home
        Tab.History -> History
        Tab.Stats -> Stats
        Tab.Profile -> Profile
    }
    navigate(route) {
        // Anchor each tab's back stack on Home (the tab root), not the graph start — the graph now
        // starts at Login, which is popped once the user enters the app.
        popUpTo(Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
