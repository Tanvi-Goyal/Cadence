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

val MindSetIcons.Timer: ImageVector
    get() {
        if (_Timer != null) {
            return _Timer!!
        }
        _Timer = ImageVector.Builder(
            name = "Timer",
            defaultWidth = 18.dp,
            defaultHeight = 21.dp,
            viewportWidth = 18f,
            viewportHeight = 21f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(6f, 2f)
                verticalLineTo(0f)
                horizontalLineTo(12f)
                verticalLineTo(2f)
                horizontalLineTo(6f)
                verticalLineTo(2f)
                moveTo(8f, 13f)
                horizontalLineTo(10f)
                verticalLineTo(7f)
                horizontalLineTo(8f)
                verticalLineTo(13f)
                verticalLineTo(13f)
                moveTo(9f, 21f)
                curveTo(7.767f, 21f, 6.604f, 20.763f, 5.512f, 20.288f)
                curveTo(4.421f, 19.813f, 3.467f, 19.167f, 2.65f, 18.35f)
                curveTo(1.833f, 17.533f, 1.188f, 16.579f, 0.712f, 15.488f)
                curveTo(0.237f, 14.396f, 0f, 13.233f, 0f, 12f)
                curveTo(0f, 10.767f, 0.237f, 9.604f, 0.712f, 8.512f)
                curveTo(1.188f, 7.421f, 1.833f, 6.467f, 2.65f, 5.65f)
                curveTo(3.467f, 4.833f, 4.421f, 4.188f, 5.512f, 3.713f)
                curveTo(6.604f, 3.237f, 7.767f, 3f, 9f, 3f)
                curveTo(10.033f, 3f, 11.025f, 3.167f, 11.975f, 3.5f)
                curveTo(12.925f, 3.833f, 13.817f, 4.317f, 14.65f, 4.95f)
                lineTo(16.05f, 3.55f)
                lineTo(17.45f, 4.95f)
                lineTo(16.05f, 6.35f)
                curveTo(16.683f, 7.183f, 17.167f, 8.075f, 17.5f, 9.025f)
                curveTo(17.833f, 9.975f, 18f, 10.967f, 18f, 12f)
                curveTo(18f, 13.233f, 17.763f, 14.396f, 17.288f, 15.488f)
                curveTo(16.813f, 16.579f, 16.167f, 17.533f, 15.35f, 18.35f)
                curveTo(14.533f, 19.167f, 13.579f, 19.813f, 12.488f, 20.288f)
                curveTo(11.396f, 20.763f, 10.233f, 21f, 9f, 21f)
                verticalLineTo(21f)
                moveTo(9f, 19f)
                curveTo(10.933f, 19f, 12.583f, 18.317f, 13.95f, 16.95f)
                curveTo(15.317f, 15.583f, 16f, 13.933f, 16f, 12f)
                curveTo(16f, 10.067f, 15.317f, 8.417f, 13.95f, 7.05f)
                curveTo(12.583f, 5.683f, 10.933f, 5f, 9f, 5f)
                curveTo(7.067f, 5f, 5.417f, 5.683f, 4.05f, 7.05f)
                curveTo(2.683f, 8.417f, 2f, 10.067f, 2f, 12f)
                curveTo(2f, 13.933f, 2.683f, 15.583f, 4.05f, 16.95f)
                curveTo(5.417f, 18.317f, 7.067f, 19f, 9f, 19f)
                verticalLineTo(19f)
            }
        }.build()

        return _Timer!!
    }

@Suppress("ObjectPropertyName")
private var _Timer: ImageVector? = null

@Preview
@Composable
private fun TimerPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.Timer, contentDescription = null)
    }
}
