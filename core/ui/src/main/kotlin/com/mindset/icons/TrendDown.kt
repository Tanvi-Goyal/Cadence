package com.mindset.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Falling counterpart to [TrendUp] — the "slower than last time" marker. */
val MindSetIcons.TrendDown: ImageVector
    get() {
        if (_TrendDown != null) {
            return _TrendDown!!
        }
        _TrendDown = ImageVector.Builder(
            name = "TrendDown",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            // Arrowhead in the bottom-right corner.
            path(fill = SolidColor(Color.White)) {
                moveTo(16f, 7f)
                verticalLineTo(13f)
                horizontalLineTo(10f)
                verticalLineTo(11f)
                horizontalLineTo(14f)
                verticalLineTo(7f)
                close()
            }
            // Shaft: up-left to down-right, mirroring TrendUp's dip as a rise.
            path(fill = SolidColor(Color.White)) {
                moveTo(0.7f, 4.1f)
                lineTo(5.5f, 8.9f)
                lineTo(8.3f, 6.1f)
                lineTo(14.3f, 12.1f)
                lineTo(15.7f, 10.7f)
                lineTo(8.3f, 3.3f)
                lineTo(5.5f, 6.1f)
                lineTo(2.1f, 2.7f)
                close()
            }
        }.build()

        return _TrendDown!!
    }

@Suppress("ObjectPropertyName")
private var _TrendDown: ImageVector? = null

@Preview
@Composable
private fun TrendDownPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.TrendDown, contentDescription = null)
    }
}
