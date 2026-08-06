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

val MindSetIcons.Search: ImageVector
    get() {
        if (_Search != null) {
            return _Search!!
        }
        _Search = ImageVector.Builder(
            name = "Search",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(16.6f, 18f)
                lineTo(10.3f, 11.7f)
                curveTo(9.8f, 12.1f, 9.225f, 12.417f, 8.575f, 12.65f)
                curveTo(7.925f, 12.883f, 7.233f, 13f, 6.5f, 13f)
                curveTo(4.683f, 13f, 3.146f, 12.371f, 1.888f, 11.113f)
                curveTo(0.629f, 9.854f, 0f, 8.317f, 0f, 6.5f)
                curveTo(0f, 4.683f, 0.629f, 3.146f, 1.888f, 1.888f)
                curveTo(3.146f, 0.629f, 4.683f, 0f, 6.5f, 0f)
                curveTo(8.317f, 0f, 9.854f, 0.629f, 11.113f, 1.888f)
                curveTo(12.371f, 3.146f, 13f, 4.683f, 13f, 6.5f)
                curveTo(13f, 7.233f, 12.883f, 7.925f, 12.65f, 8.575f)
                curveTo(12.417f, 9.225f, 12.1f, 9.8f, 11.7f, 10.3f)
                lineTo(18f, 16.6f)
                lineTo(16.6f, 18f)
                verticalLineTo(18f)
                moveTo(6.5f, 11f)
                curveTo(7.75f, 11f, 8.813f, 10.563f, 9.688f, 9.688f)
                curveTo(10.563f, 8.813f, 11f, 7.75f, 11f, 6.5f)
                curveTo(11f, 5.25f, 10.563f, 4.188f, 9.688f, 3.313f)
                curveTo(8.813f, 2.438f, 7.75f, 2f, 6.5f, 2f)
                curveTo(5.25f, 2f, 4.188f, 2.438f, 3.313f, 3.313f)
                curveTo(2.438f, 4.188f, 2f, 5.25f, 2f, 6.5f)
                curveTo(2f, 7.75f, 2.438f, 8.813f, 3.313f, 9.688f)
                curveTo(4.188f, 10.563f, 5.25f, 11f, 6.5f, 11f)
                verticalLineTo(11f)
            }
        }.build()

        return _Search!!
    }

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null

@Preview
@Composable
private fun SearchPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.Search, contentDescription = null)
    }
}
