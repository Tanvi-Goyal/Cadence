package com.mindset

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindset.components.SheenProgress
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** How long the splash holds before advancing. Placeholder value — see the roadmap for real gating. */
const val SPLASH_DURATION_MS = 1000L

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS.milliseconds)
        onDone()
    }

    MindSetTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppBrand()
                Spacer(Modifier.height(36.dp))
                SheenProgress(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp),
                )
            }
        }
    }
}
