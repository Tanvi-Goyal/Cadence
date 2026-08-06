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

val MindSetIcons.Flame: ImageVector
    get() {
        if (_Flame != null) {
            return _Flame!!
        }
        _Flame = ImageVector.Builder(
            name = "Flame",
            defaultWidth = 12.dp,
            defaultHeight = 14.25.dp,
            viewportWidth = 12f,
            viewportHeight = 14.25f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(1.5f, 8.25f)
                curveTo(1.5f, 8.9f, 1.631f, 9.516f, 1.894f, 10.097f)
                curveTo(2.156f, 10.678f, 2.531f, 11.188f, 3.019f, 11.625f)
                curveTo(3.006f, 11.563f, 3f, 11.506f, 3f, 11.456f)
                curveTo(3f, 11.406f, 3f, 11.35f, 3f, 11.288f)
                curveTo(3f, 10.887f, 3.075f, 10.512f, 3.225f, 10.163f)
                curveTo(3.375f, 9.813f, 3.594f, 9.494f, 3.881f, 9.206f)
                lineTo(6f, 7.125f)
                lineTo(8.119f, 9.206f)
                curveTo(8.406f, 9.494f, 8.625f, 9.813f, 8.775f, 10.163f)
                curveTo(8.925f, 10.512f, 9f, 10.887f, 9f, 11.288f)
                curveTo(9f, 11.35f, 9f, 11.406f, 9f, 11.456f)
                curveTo(9f, 11.506f, 8.994f, 11.563f, 8.981f, 11.625f)
                curveTo(9.469f, 11.188f, 9.844f, 10.678f, 10.106f, 10.097f)
                curveTo(10.369f, 9.516f, 10.5f, 8.9f, 10.5f, 8.25f)
                curveTo(10.5f, 7.625f, 10.384f, 7.034f, 10.153f, 6.478f)
                curveTo(9.922f, 5.922f, 9.587f, 5.425f, 9.15f, 4.988f)
                curveTo(8.9f, 5.15f, 8.637f, 5.272f, 8.363f, 5.353f)
                curveTo(8.087f, 5.434f, 7.806f, 5.475f, 7.519f, 5.475f)
                curveTo(6.744f, 5.475f, 6.072f, 5.219f, 5.503f, 4.706f)
                curveTo(4.934f, 4.194f, 4.606f, 3.563f, 4.519f, 2.813f)
                curveTo(4.031f, 3.225f, 3.6f, 3.653f, 3.225f, 4.097f)
                curveTo(2.85f, 4.541f, 2.534f, 4.991f, 2.278f, 5.447f)
                curveTo(2.022f, 5.903f, 1.828f, 6.369f, 1.697f, 6.844f)
                curveTo(1.566f, 7.319f, 1.5f, 7.787f, 1.5f, 8.25f)
                verticalLineTo(8.25f)
                moveTo(6f, 9.225f)
                lineTo(4.931f, 10.275f)
                curveTo(4.794f, 10.413f, 4.688f, 10.569f, 4.613f, 10.744f)
                curveTo(4.537f, 10.919f, 4.5f, 11.1f, 4.5f, 11.288f)
                curveTo(4.5f, 11.688f, 4.647f, 12.031f, 4.941f, 12.319f)
                curveTo(5.234f, 12.606f, 5.588f, 12.75f, 6f, 12.75f)
                curveTo(6.412f, 12.75f, 6.766f, 12.606f, 7.059f, 12.319f)
                curveTo(7.353f, 12.031f, 7.5f, 11.688f, 7.5f, 11.288f)
                curveTo(7.5f, 11.087f, 7.463f, 10.903f, 7.387f, 10.734f)
                curveTo(7.313f, 10.566f, 7.206f, 10.413f, 7.069f, 10.275f)
                lineTo(6f, 9.225f)
                verticalLineTo(9.225f)
                moveTo(6f, 0f)
                verticalLineTo(2.475f)
                curveTo(6f, 2.9f, 6.147f, 3.256f, 6.441f, 3.544f)
                curveTo(6.734f, 3.831f, 7.094f, 3.975f, 7.519f, 3.975f)
                curveTo(7.744f, 3.975f, 7.953f, 3.928f, 8.147f, 3.834f)
                curveTo(8.341f, 3.741f, 8.512f, 3.6f, 8.663f, 3.412f)
                lineTo(9f, 3f)
                curveTo(9.925f, 3.525f, 10.656f, 4.256f, 11.194f, 5.194f)
                curveTo(11.731f, 6.131f, 12f, 7.15f, 12f, 8.25f)
                curveTo(12f, 9.925f, 11.419f, 11.344f, 10.256f, 12.506f)
                curveTo(9.094f, 13.669f, 7.675f, 14.25f, 6f, 14.25f)
                curveTo(4.325f, 14.25f, 2.906f, 13.669f, 1.744f, 12.506f)
                curveTo(0.581f, 11.344f, 0f, 9.925f, 0f, 8.25f)
                curveTo(0f, 6.637f, 0.541f, 5.106f, 1.622f, 3.656f)
                curveTo(2.703f, 2.206f, 4.162f, 0.988f, 6f, 0f)
                verticalLineTo(0f)
            }
        }.build()

        return _Flame!!
    }

@Suppress("ObjectPropertyName")
private var _Flame: ImageVector? = null

@Preview
@Composable
private fun FlamePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.Flame, contentDescription = null)
    }
}
