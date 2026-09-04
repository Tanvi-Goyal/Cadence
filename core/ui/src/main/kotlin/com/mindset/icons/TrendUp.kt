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

/**
 * Rising polyline with an arrowhead — the "improving" marker on the Station board's trend chip.
 * Paired with [TrendDown]; drawn as its own vector rather than a flipped copy so the arrowhead stays
 * in the corner it points at.
 */
val MindSetIcons.TrendUp: ImageVector
    get() {
        if (_TrendUp != null) {
            return _TrendUp!!
        }
        _TrendUp = ImageVector.Builder(
            name = "TrendUp",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            // Arrowhead in the top-right corner.
            path(fill = SolidColor(Color.White)) {
                moveTo(10f, 3f)
                horizontalLineTo(16f)
                verticalLineTo(9f)
                horizontalLineTo(14f)
                verticalLineTo(5f)
                horizontalLineTo(10f)
                close()
            }
            // Shaft: down-left to up-right, with a dip so it reads as a trend line, not a plain arrow.
            path(fill = SolidColor(Color.White)) {
                moveTo(0.7f, 11.9f)
                lineTo(5.5f, 7.1f)
                lineTo(8.3f, 9.9f)
                lineTo(14.3f, 3.9f)
                lineTo(15.7f, 5.3f)
                lineTo(8.3f, 12.7f)
                lineTo(5.5f, 9.9f)
                lineTo(2.1f, 13.3f)
                close()
            }
        }.build()

        return _TrendUp!!
    }

@Suppress("ObjectPropertyName")
private var _TrendUp: ImageVector? = null

@Preview
@Composable
private fun TrendUpPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.TrendUp, contentDescription = null)
    }
}
