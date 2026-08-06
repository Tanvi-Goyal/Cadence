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
import dev.cadence.ui.R

/**
 * The MIND[SET] wordmark — "MIND"/"SET" in white, the brackets tinted `primary`. Shared by the vertical
 * [AppBrand] lockup and the horizontal [dev.cadence.components.CadenceTopBar] so the two never diverge.
 */
@Composable
fun CadenceWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
) {
    val primary = MaterialTheme.colorScheme.primary
    val appName = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.White)) { append("MIND") }
        withStyle(SpanStyle(color = primary)) { append("[") }
        withStyle(SpanStyle(color = Color.White)) { append("SET") }
        withStyle(SpanStyle(color = primary)) { append("]") }
    }
    Text(text = appName, style = style, modifier = modifier)
}

@Composable
fun AppBrand(
    modifier: Modifier = Modifier,
    logoSize: Dp = 108.dp,
    gap: Dp = 28.dp,
    showWordmark: Boolean = true,
    wordmarkStyle: TextStyle = MaterialTheme.typography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
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
            CadenceWordmark(style = wordmarkStyle)
        }
    }
}
