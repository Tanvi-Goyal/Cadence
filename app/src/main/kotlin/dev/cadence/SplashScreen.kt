package dev.cadence

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
const val SPLASH_DURATION_MS = 2500L

private val BrandBg = Color(0xFF0F0F11)

@Composable
fun SplashScreen(onDone: () -> Unit) {
    // Hold for the splash duration, then advance. The hand-off motion (fade / slide-up into
    // Onboarding) is a coordinated NavHost transition (see SplashNavGraph / OnboardingNavGraph), not
    // an internal animation, so it only plays on the onboarding path and stays smooth.
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onDone()
    }

    // Wrap in CadenceTheme so the wordmark renders in the app's Inter type scale (the per-screen
    // theme wrap is the established pattern; nesting is idempotent). Colors here are the logo's own.
    CadenceTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(BrandBg),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BrandLockup()
                Spacer(Modifier.height(36.dp))
                SheenProgress(modifier = Modifier.width(200.dp).height(4.dp))
            }
        }
    }
}

/**
 * A premium "sheen" progress line: a lens-shaped stroke (pointed at both ends, thickest in the
 * middle) filled with a soft whitish gradient that fades into the background at both edges, with a
 * brighter highlight that sweeps across it on a loop. Purely decorative — indicates activity while
 * the splash holds. No red; the glow is white on the near-black brand background.
 */
@Composable
private fun SheenProgress(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sheen")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweep",
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2f

        // Lens outline: two mirrored quadratics meeting in points at each end, thickest (h) at center.
        val lens = Path().apply {
            moveTo(0f, cy)
            quadraticBezierTo(w * 0.5f, cy - h, w, cy)
            quadraticBezierTo(w * 0.5f, cy + h, 0f, cy)
            close()
        }

        clipPath(lens) {
            // Static base sheen: dim white, brightest at center, fading to transparent at both ends.
            drawRect(
                brush = Brush.horizontalGradient(
                    0.0f to Color.Transparent,
                    0.5f to Color.White.copy(alpha = 0.45f),
                    1.0f to Color.Transparent,
                ),
                size = size,
            )
            // Traveling highlight: a soft bright band that sweeps left -> right and loops.
            val center = (-0.3f + 1.6f * phase) * w
            val half = w * 0.24f
            drawRect(
                brush = Brush.horizontalGradient(
                    0.0f to Color.Transparent,
                    0.5f to Color.White.copy(alpha = 0.95f),
                    1.0f to Color.Transparent,
                    startX = center - half,
                    endX = center + half,
                ),
                size = size,
            )
        }
    }
}
