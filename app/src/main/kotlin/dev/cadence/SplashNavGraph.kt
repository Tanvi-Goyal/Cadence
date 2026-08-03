package dev.cadence

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/*
 * Splash destination the app graph contributes at start-up. Mirrors the feature nav-graph pattern
 * (see loginScreen in feature/auth): the app supplies the onward nav action as a lambda so this
 * stays decoupled from the NavHost. [Splash] is the graph's start destination.
 */
fun NavGraphBuilder.splashScreen(onDone: () -> Unit) {
    composable<Splash> {
        SplashScreen(onDone = onDone)
    }
}
