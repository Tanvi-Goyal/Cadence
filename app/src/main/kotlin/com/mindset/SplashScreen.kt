package com.mindset

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.mindset.components.SheenProgress
import kotlinx.coroutines.delay

/*
 * Branded cold-start splash (the app's start destination). Shows the shared [BrandLockup] (icon glyph
 * + MIND[SET] wordmark) and a minimal sheen progress line that shimmers while the splash holds, then
 * calls [onDone] after [SPLASH_DURATION_MS].
 *
 * The lockup is the SAME element the Onboarding header renders, so a future slide-up transition can
 * carry it between the two screens. The palette is the logo's own (near-black + white/red), so this
 * screen hardcodes its background rather than reading MaterialTheme roles.
 */

/** How long the splash holds before advancing. Placeholder value — see the roadmap for real gating. */
const val SPLASH_DURATION_MS = 1500L

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
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
                SheenProgress(modifier = Modifier
                    .width(200.dp)
                    .height(4.dp))
            }
        }
    }
}
