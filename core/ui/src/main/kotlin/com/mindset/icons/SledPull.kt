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

val MindSetIcons.SledPull: ImageVector
    get() {
        if (_SledPull != null) {
            return _SledPull!!
        }
        _SledPull = ImageVector.Builder(
            name = "SledPull",
            defaultWidth = 15.994.dp,
            defaultHeight = 18.dp,
            viewportWidth = 15.994f,
            viewportHeight = 18f,
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(1.997f, 16f)
                horizontalLineTo(13.997f)
                lineTo(12.572f, 6f)
                horizontalLineTo(3.422f)
                lineTo(1.997f, 16f)
                moveTo(7.997f, 4f)
                curveTo(8.28f, 4f, 8.518f, 3.904f, 8.709f, 3.713f)
                curveTo(8.901f, 3.521f, 8.997f, 3.283f, 8.997f, 3f)
                curveTo(8.997f, 2.717f, 8.901f, 2.479f, 8.709f, 2.287f)
                curveTo(8.518f, 2.096f, 8.28f, 2f, 7.997f, 2f)
                curveTo(7.714f, 2f, 7.476f, 2.096f, 7.284f, 2.287f)
                curveTo(7.093f, 2.479f, 6.997f, 2.717f, 6.997f, 3f)
                curveTo(6.997f, 3.283f, 7.093f, 3.521f, 7.284f, 3.713f)
                curveTo(7.476f, 3.904f, 7.714f, 4f, 7.997f, 4f)
                moveTo(10.822f, 4f)
                horizontalLineTo(12.572f)
                curveTo(13.072f, 4f, 13.505f, 4.167f, 13.872f, 4.5f)
                curveTo(14.239f, 4.833f, 14.464f, 5.242f, 14.547f, 5.725f)
                lineTo(15.972f, 15.725f)
                curveTo(16.055f, 16.325f, 15.901f, 16.854f, 15.509f, 17.313f)
                curveTo(15.118f, 17.771f, 14.614f, 18f, 13.997f, 18f)
                horizontalLineTo(1.997f)
                curveTo(1.38f, 18f, 0.876f, 17.771f, 0.484f, 17.313f)
                curveTo(0.093f, 16.854f, -0.061f, 16.325f, 0.022f, 15.725f)
                lineTo(1.447f, 5.725f)
                curveTo(1.53f, 5.242f, 1.755f, 4.833f, 2.122f, 4.5f)
                curveTo(2.489f, 4.167f, 2.922f, 4f, 3.422f, 4f)
                horizontalLineTo(5.172f)
                curveTo(5.122f, 3.833f, 5.08f, 3.671f, 5.047f, 3.513f)
                curveTo(5.014f, 3.354f, 4.997f, 3.183f, 4.997f, 3f)
                curveTo(4.997f, 2.167f, 5.289f, 1.458f, 5.872f, 0.875f)
                curveTo(6.455f, 0.292f, 7.164f, 0f, 7.997f, 0f)
                curveTo(8.83f, 0f, 9.539f, 0.292f, 10.122f, 0.875f)
                curveTo(10.705f, 1.458f, 10.997f, 2.167f, 10.997f, 3f)
                curveTo(10.997f, 3.183f, 10.98f, 3.354f, 10.947f, 3.513f)
                curveTo(10.914f, 3.671f, 10.872f, 3.833f, 10.822f, 4f)
                verticalLineTo(4f)
            }
        }.build()

        return _SledPull!!
    }

@Suppress("ObjectPropertyName")
private var _SledPull: ImageVector? = null

@Preview
@Composable
private fun SledPullPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.SledPull, contentDescription = null)
    }
}
