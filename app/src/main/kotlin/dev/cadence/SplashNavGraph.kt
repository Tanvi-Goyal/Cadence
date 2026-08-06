package dev.cadence

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.cadence.domain.repository.AthleteProfileRepository
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

/*
 * Splash destination the app graph contributes at start-up. Mirrors the feature nav-graph pattern
 * (see loginScreen in feature/auth): the app supplies the onward nav action as a lambda so this
 * stays decoupled from the NavHost. [Splash] is the graph's start destination.
 *
 * It reads the persisted onboarding flag and hands it to [onDone], so the host can route a first-run
 * user to Onboarding and a returning user straight to Home. Exits with a fade so the brand lockup
 * (which the splash slides to the top) crossfades into the Onboarding header.
 */
fun NavGraphBuilder.splashScreen(onDone: (onboardingComplete: Boolean) -> Unit) {
    composable<Splash>(
        exitTransition = { fadeOut(tween(durationMillis = 360)) },
    ) {
        val repository = koinInject<AthleteProfileRepository>()
        val onboardingComplete by remember(repository) {
            repository.observe().map { it.onboardingComplete }
        }.collectAsStateWithLifecycle(initialValue = false)

        SplashScreen(onDone = { onDone(onboardingComplete) })
    }
}
