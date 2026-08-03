package dev.cadence

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Onboarding destination. The host supplies [onComplete] (navigate to Home + pop Onboarding).
 * Enters by rising up from just below the fold + fading in (over the splash's fade-out) — the
 * smooth "slide-up" hand-off. This transition plays only when entering Onboarding, so a returning
 * user routed straight to Home never sees it.
 */
fun NavGraphBuilder.onboardingScreen(onComplete: () -> Unit) {
    val spec = tween<Float>(durationMillis = 460, easing = FastOutSlowInEasing)
    composable<Onboarding>(
        enterTransition = {
            slideInVertically(
                animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing),
                initialOffsetY = { fullHeight -> fullHeight / 5 },
            ) + fadeIn(spec)
        },
    ) {
        OnboardingScreen(onComplete = onComplete)
    }
}
