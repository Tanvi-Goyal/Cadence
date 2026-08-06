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

val MindSetIcons.Grid: ImageVector
    get() {
        if (_Grid != null) {
            return _Grid!!
        }
        _Grid = ImageVector.Builder(
            name = "Grid",
            defaultWidth = 20.dp,
            defaultHeight = 16.dp,
            viewportWidth = 20f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(2f, 7f)
                curveTo(1.45f, 7f, 0.979f, 6.804f, 0.587f, 6.412f)
                curveTo(0.196f, 6.021f, 0f, 5.55f, 0f, 5f)
                verticalLineTo(2f)
                curveTo(0f, 1.45f, 0.196f, 0.979f, 0.587f, 0.587f)
                curveTo(0.979f, 0.196f, 1.45f, 0f, 2f, 0f)
                horizontalLineTo(11f)
                verticalLineTo(7f)
                horizontalLineTo(2f)
                verticalLineTo(7f)
                moveTo(2f, 5f)
                horizontalLineTo(9f)
                verticalLineTo(2f)
                horizontalLineTo(2f)
                verticalLineTo(2f)
                verticalLineTo(2f)
                verticalLineTo(5f)
                verticalLineTo(5f)
                verticalLineTo(5f)
                verticalLineTo(5f)
                moveTo(2f, 16f)
                curveTo(1.45f, 16f, 0.979f, 15.804f, 0.587f, 15.413f)
                curveTo(0.196f, 15.021f, 0f, 14.55f, 0f, 14f)
                verticalLineTo(11f)
                curveTo(0f, 10.45f, 0.196f, 9.979f, 0.587f, 9.587f)
                curveTo(0.979f, 9.196f, 1.45f, 9f, 2f, 9f)
                horizontalLineTo(13f)
                verticalLineTo(16f)
                horizontalLineTo(2f)
                verticalLineTo(16f)
                moveTo(2f, 14f)
                horizontalLineTo(11f)
                verticalLineTo(11f)
                horizontalLineTo(2f)
                verticalLineTo(11f)
                verticalLineTo(11f)
                verticalLineTo(14f)
                verticalLineTo(14f)
                verticalLineTo(14f)
                verticalLineTo(14f)
                moveTo(15f, 16f)
                verticalLineTo(7f)
                horizontalLineTo(13f)
                verticalLineTo(0f)
                horizontalLineTo(20f)
                lineTo(18f, 5f)
                horizontalLineTo(20f)
                lineTo(15f, 16f)
                verticalLineTo(16f)
                moveTo(2.75f, 13.25f)
                horizontalLineTo(4.25f)
                verticalLineTo(11.75f)
                horizontalLineTo(2.75f)
                verticalLineTo(13.25f)
                verticalLineTo(13.25f)
                moveTo(2.75f, 4.25f)
                horizontalLineTo(4.25f)
                verticalLineTo(2.75f)
                horizontalLineTo(2.75f)
                verticalLineTo(4.25f)
                verticalLineTo(4.25f)
            }
        }.build()

        return _Grid!!
    }

@Suppress("ObjectPropertyName")
private var _Grid: ImageVector? = null

@Preview
@Composable
private fun GridPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.Grid, contentDescription = null)
    }
}
