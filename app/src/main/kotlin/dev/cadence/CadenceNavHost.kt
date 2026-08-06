package dev.cadence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.cadence.domain.SessionRepository
import dev.cadence.model.SessionType
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CadenceNavHost() {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val sessionRepository = koinInject<SessionRepository>()

    CompositionLocalProvider(
        LocalQuickStart provides {
            scope.launch {
                val session = sessionRepository.createSession(SessionType.STRENGTH.name)
                nav.navigate(LogWorkout(session.id))
            }
        },
    ) {

    NavHost(
        navController = nav,
        startDestination = Splash
    ) {

        splashScreen(
            onDone = { onboardingComplete ->
                val destination: Any = if (onboardingComplete) Home else Onboarding
                nav.navigate(destination) { popUpTo(Splash) { inclusive = true } }
            },
        )

        onboardingScreen(
            onComplete = { nav.navigate(Home) { popUpTo(Onboarding) { inclusive = true } } },
        )

        homeScreen(
            onOpenSession = { id -> nav.navigate(LogWorkout(id)) },
            onNewSession = { nav.navigate(NewSession) },
            onOpenTemplates = { nav.navigate(Templates) },
            onOpenTemplate = { id -> nav.openTemplateDetail(id) },
            onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
            onSeeAll = { nav.switchTab(Tab.History) },
            onTab = nav::switchTab,
        )

        loginScreen(
            onSignedIn = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
            onCreateAccount = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
            onForgotPassword = {},
        )

        historyScreen(
            onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
            onOpenProfile = { nav.switchTab(Tab.Profile) },
            onTab = nav::switchTab,
        )

        statsScreen(onTab = nav::switchTab)

        profileScreen(onTab = nav::switchTab, onOpenCredits = { nav.navigate(Credits) })

        creditsScreen(onBack = { nav.popBackStack() })

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

        templatesScreen(
            onBack = { nav.popBackStack() },
            onNewTemplate = { nav.navigate(NewTemplate) },
            onEditTemplate = { id -> nav.openTemplateDetail(id) },
            onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
        )

        templateDetailScreen(
            onBack = { nav.popBackStack() },
            onStart = { nav.navigate(NewSession) },
            onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
        )
        templateHyroxDetailScreen(
            onBack = { nav.popBackStack() },
            onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
        )
        templateStrengthDetailScreen(
            onBack = { nav.popBackStack() },
            // Start deep-copies the template into a live session; open it in Log Workout.
            onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
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
}

private fun NavController.openTemplateDetail(id: String) {
    when {
        id == "full-hyrox-simulation" || id == "half-hyrox-sim" -> navigate(TemplateHyroxDetail(id))
        id.startsWith("hyfit-") -> navigate(TemplateStrengthDetail(id))
        else -> navigate(TemplateDetail(id))
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
        Tab.Stations -> Stats
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
