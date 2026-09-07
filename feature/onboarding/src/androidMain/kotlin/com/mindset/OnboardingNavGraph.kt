package com.mindset

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

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
