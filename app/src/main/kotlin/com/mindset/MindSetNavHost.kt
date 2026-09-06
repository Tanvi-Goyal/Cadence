package com.mindset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mindset.components.LocalQuickStart
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.BottomNavTab
import com.mindset.model.SessionType
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MindSetNavHost() {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val sessionRepository = koinInject<SessionRepository>()

    val quickStart: () -> Unit = {
        scope.launch {
            val session = sessionRepository.createSession(SessionType.STRENGTH.name)
            nav.navigate(LogWorkout(session.id))
        }
    }

    CompositionLocalProvider(
        LocalQuickStart provides quickStart,
    ) {
        NavHost(
            navController = nav,
            startDestination = Splash,
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

            loginScreen(
                onSignedIn = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
                onCreateAccount = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
                onForgotPassword = {},
            )

            homeScreen(
                onOpenTemplates = { nav.navigate(Templates) },
                onOpenTemplate = { id -> nav.openTemplateDetail(id) },
                onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
                onSeeAll = { nav.navigate(History) },
                onTab = nav::switchTab,
            )

            // History is no longer a tab — it is a push from Home's "See all", so it carries a back
            // arrow and no bottom bar.
            historyScreen(
                onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
                onBack = { nav.popBackStack() },
                onOpenProfile = { nav.switchTab(BottomNavTab.Profile) },
            )

            stationsScreen(onTab = nav::switchTab)

            profileScreen(onTab = nav::switchTab)

            creditsScreen(onBack = { nav.popBackStack() })

            logTabScreen(
                onAddExercise = { nav.navigate(ExercisePicker(PickerTarget.LOG_TAB)) },
                onTab = nav::switchTab,
                // Completing from the tab lands on Home, where the session now shows under Recent.
                onCompleted = { nav.switchTab(BottomNavTab.Home) },
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
                            target,
                        ),
                    )
                },
                onBack = { nav.popBackStack() },
            )

            exerciseDetailScreen(
                onAdd = { exerciseId, target ->
                    val origin = when (target) {
                        PickerTarget.LOG -> nav.getBackStackEntry<LogWorkout>()
                        PickerTarget.LOG_TAB -> nav.getBackStackEntry<Log>()
                        PickerTarget.BUILDER -> nav.getBackStackEntry<TemplateBuilder>()
                    }
                    origin.savedStateHandle[PICKED_EXERCISE] = exerciseId
                    when (target) {
                        PickerTarget.LOG -> nav.popBackStack<LogWorkout>(inclusive = false)
                        PickerTarget.LOG_TAB -> nav.popBackStack<Log>(inclusive = false)
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
private fun NavController.switchTab(tab: BottomNavTab) {
    val route: Any = when (tab) {
        BottomNavTab.Home -> Home
        BottomNavTab.Log -> Log
        BottomNavTab.Stations -> Stations
        BottomNavTab.Profile -> Profile
    }
    navigate(route) {
        // Anchor each tab's back stack on Home (the tab root), not the graph start — the graph now
        // starts at Login, which is popped once the user enters the app.
        popUpTo(Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
