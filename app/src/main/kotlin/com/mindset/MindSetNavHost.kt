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
                onComplete = {
                    nav.navigate(Home) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                },
            )

            homeScreen(
                // TODO(phase2): restore `{ nav.navigate(Templates) }` with the template library.
                onOpenTemplate = { id -> nav.openTemplateDetail(id) },
                onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
                onSeeAll = { nav.navigate(History) },
                onTab = nav::switchTab,
            )

            logTabScreen(
                // TODO(phase2): restore `{ nav.navigate(ExercisePicker(PickerTarget.LOG_TAB)) }`.
                onTab = nav::switchTab,
                onCompleted = { nav.switchTab(BottomNavTab.Home) },
            )

            stationsScreen(onTab = nav::switchTab)

            profileScreen(onTab = nav::switchTab)

            templateHyroxDetailScreen(
                onBack = { nav.popBackStack() },
                // TODO(phase2): restore `{ id -> nav.navigate(TemplateBuilder(id)) }` with the builder.
                onEdit = {},
            )

            sessionDetailScreen(onBack = { nav.popBackStack() })

            historyScreen(
                onOpenDetail = { id -> nav.navigate(SessionDetail(id)) },
                onBack = { nav.popBackStack() },
                onOpenProfile = { nav.switchTab(BottomNavTab.Profile) },
            )

//            creditsScreen(onBack = { nav.popBackStack() })
//
//            loginScreen(
//                onSignedIn = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
//                onCreateAccount = { nav.navigate(Home) { popUpTo(Login) { inclusive = true } } },
//                onForgotPassword = {},
//            )
//
//            logWorkoutScreen(
//                onBack = { nav.popBackStack() },
//                // TODO(phase2): restore `{ nav.navigate(ExercisePicker(PickerTarget.LOG)) }`.
//                onAddExercise = {},
//                onFinish = { nav.popBackStack(Home, inclusive = false) },
//            )

            // TODO(phase2): re-enable, cut from v1 scope — the exercise catalog browser. v1 logs
            // Hyrox stations only (AddToSessionSheet). The catalog itself stays seeded: session
            // detail, the Log screen and the Stations PB board all resolve exercise names from it.
            // Restoring these also needs LoggingNavGraph's PICKED_EXERCISE savedStateHandle read
            // re-enabled, and LogWorkoutScreen rendering items whose segmentKey is null.
//            exercisePickerScreen(
//                onOpenDetail = { exerciseId, target ->
//                    nav.navigate(
//                        ExerciseDetail(
//                            exerciseId,
//                            target,
//                        ),
//                    )
//                },
//                onBack = { nav.popBackStack() },
//            )
//
//            exerciseDetailScreen(
//                onAdd = { exerciseId, target ->
//                    val origin = when (target) {
//                        PickerTarget.LOG -> nav.getBackStackEntry<LogWorkout>()
//                        PickerTarget.LOG_TAB -> nav.getBackStackEntry<Log>()
//                        PickerTarget.BUILDER -> nav.getBackStackEntry<TemplateBuilder>()
//                    }
//                    origin.savedStateHandle[PICKED_EXERCISE] = exerciseId
//                    when (target) {
//                        PickerTarget.LOG -> nav.popBackStack<LogWorkout>(inclusive = false)
//                        PickerTarget.LOG_TAB -> nav.popBackStack<Log>(inclusive = false)
//                        PickerTarget.BUILDER -> nav.popBackStack<TemplateBuilder>(inclusive = false)
//                    }
//                },
//                onBack = { nav.popBackStack() },
//            )


            // TODO(phase2): re-enable, cut from v1 scope — the template library and its editor.
            // v1 ships the Hyrox race simulation only, which enters via templateHyroxDetailScreen
            // below. Restoring these also needs a `composable<NewSession>` registration, which
            // templateDetailScreen's onStart navigates to and which never existed.
//            templatesScreen(
//                onBack = { nav.popBackStack() },
//                onNewTemplate = { nav.navigate(NewTemplate) },
//                onEditTemplate = { id -> nav.openTemplateDetail(id) },
//                onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
//            )
//
//            templateDetailScreen(
//                onBack = { nav.popBackStack() },
//                onStart = { nav.navigate(NewSession) },
//                onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
//            )

            // TODO(phase2): re-enable, cut from v1 scope.
//            templateStrengthDetailScreen(
//                onBack = { nav.popBackStack() },
//                // Start deep-copies the template into a live session; open it in Log Workout.
//                onStarted = { sessionId -> nav.navigate(LogWorkout(sessionId)) },
//                onEdit = { id -> nav.navigate(TemplateBuilder(id)) },
//            )
//            newTemplateScreen(
//                onBack = { nav.popBackStack() },
//                onCreated = { id ->
//                    // Replace New Template with the builder so Back returns to the Templates list.
//                    nav.navigate(TemplateBuilder(id)) { popUpTo<NewTemplate> { inclusive = true } }
//                },
//            )
//            templateBuilderScreen(
//                onBack = { nav.popBackStack() },
//                onAddExercise = { nav.navigate(ExercisePicker(PickerTarget.BUILDER)) },
//                onDone = { nav.popBackStack() },
//            )
        }
    }
}

/**
 * v1 has exactly one template destination: the Hyrox simulation detail. Home's Simulations rail is
 * the only caller and only ever emits the two hyrox ids, so the other branches are unreachable —
 * and their routes are no longer registered, which would make them a crash rather than a no-op.
 */
private fun NavController.openTemplateDetail(id: String) {
    navigate(TemplateHyroxDetail(id))
    // TODO(phase2): restore the full dispatch when the template library returns.
//    when {
//        id == "full-hyrox-simulation" || id == "half-hyrox-sim" -> navigate(TemplateHyroxDetail(id))
//        id.startsWith("hyfit-") -> navigate(TemplateStrengthDetail(id))
//        else -> navigate(TemplateDetail(id))
//    }
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
        popUpTo(Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
