package com.mindset.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath


/**
 * A premium "sheen" progress line: a lens-shaped stroke (pointed at both ends, thickest in the
 * middle) filled with a soft whitish gradient that fades into the background at both edges, with a
 * brighter highlight that sweeps across it on a loop. Purely decorative — indicates activity while
 * the splash holds. No red; the glow is white on the near-black brand background.
 */
@Composable
fun SheenProgress(modifier: Modifier = Modifier) {
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

        val lens = Path().apply {
            moveTo(0f, cy)
            quadraticTo(w * 0.5f, cy - h, w, cy)
            quadraticTo(w * 0.5f, cy + h, 0f, cy)
            close()
        }

        clipPath(lens) {
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
