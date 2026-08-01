package dev.cadence

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/*
 * Branded cold-start splash (the app's start destination). Shows the MIND[SET] icon glyph, the
 * wordmark, and a minimal sheen progress line that shimmers while the splash holds, then calls
 * [onDone] after [SPLASH_DURATION_MS].
 *
 * The palette is the logo's own (near-black + red), independent of the app's Kinetic Precision
 * theme, so this screen intentionally hardcodes its colors rather than reading MaterialTheme roles.
 * Only the wordmark borrows the theme's Inter type scale.
 */

/** How long the splash holds before advancing. Placeholder value — see the roadmap for real gating. */
const val SPLASH_DURATION_MS = 5_000L

private val BrandBg = Color(0xFF0F0F11)
private val BrandInk = Color(0xFFFFFFFF)
private val BrandRed = Color(0xFFE5484D)

@Composable
fun SplashScreen(onDone: () -> Unit) {
    // Hold for the splash duration, then advance. The visual below is a decorative shimmer, decoupled
    // from this timer (no real work is being gated yet — see the roadmap).
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onDone()
    }

    val wordmark = buildAnnotatedString {
        withStyle(SpanStyle(color = BrandInk)) { append("MIND") }
        withStyle(SpanStyle(color = BrandRed)) { append("[") }
        withStyle(SpanStyle(color = BrandInk)) { append("SET") }
        withStyle(SpanStyle(color = BrandRed)) { append("]") }
    }

    // Wrap in CadenceTheme so the wordmark renders in the app's Inter type scale (the per-screen
    // theme wrap is the established pattern; nesting is idempotent). Colors here are the logo's own.
    CadenceTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(BrandBg),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.brand_logo),
                    contentDescription = null, // decorative; the wordmark below names the app
                    modifier = Modifier.size(112.dp),
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = wordmark,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                    ),
                )
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
