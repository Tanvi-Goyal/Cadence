package com.mindset

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mindset.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

fun NavGraphBuilder.splashScreen(onDone: (onboardingComplete: Boolean) -> Unit) {
    composable<Splash>(
        exitTransition = { fadeOut(tween(durationMillis = 360)) },
    ) {
        val repository = koinInject<PreferencesRepository>()
        val onboardingComplete by remember(repository) {
            repository.observe().map { it.isOnboardingComplete }
        }.collectAsStateWithLifecycle(initialValue = false)

        SplashScreen(onDone = { onDone(onboardingComplete) })
    }
}
