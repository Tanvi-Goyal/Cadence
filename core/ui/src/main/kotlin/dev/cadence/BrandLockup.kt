package dev.cadence

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.cadence.ui.R

/*
 * The shared MIND[SET] brand lockup — the icon glyph above the wordmark — used by the Splash screen
 * and the Onboarding header so both render an identical element (enabling the splash→onboarding
 * slide-up). The palette is the logo's own (near-white ink + brand red), independent of the app
 * theme, so it looks the same in either design variant.
 */

private val BrandInk = Color(0xFFFFFFFF)
private val BrandRed = Color(0xFFE5484D)

/** MIND[SET], with the brackets in brand red. */
val brandWordmark = buildAnnotatedString {
    withStyle(SpanStyle(color = BrandInk)) { append("MIND") }
    withStyle(SpanStyle(color = BrandRed)) { append("[") }
    withStyle(SpanStyle(color = BrandInk)) { append("SET") }
    withStyle(SpanStyle(color = BrandRed)) { append("]") }
}

/**
 * The icon glyph stacked above the [brandWordmark]. [logoSize] and [gap] let callers scale it
 * (Splash uses the large centred form; Onboarding a smaller header form). Colours are fixed to the
 * brand palette so the element is pixel-identical across screens.
 */
@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    logoSize: Dp = 112.dp,
    gap: Dp = 28.dp,
    showWordmark: Boolean = true,
    wordmarkStyle: TextStyle = MaterialTheme.typography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1).sp,
    ),
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.brand_logo),
            contentDescription = "MIND[SET]",
            modifier = Modifier.size(logoSize),
        )
        if (showWordmark) {
            Spacer(Modifier.height(gap))
            Text(text = brandWordmark, style = wordmarkStyle)
        }
    }
}
