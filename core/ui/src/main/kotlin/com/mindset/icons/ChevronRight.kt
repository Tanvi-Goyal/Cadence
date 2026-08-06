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

val MindSetIcons.ChevronRight: ImageVector
    get() {
        if (_ChevronRight != null) {
            return _ChevronRight!!
        }
        _ChevronRight = ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 7.4.dp,
            defaultHeight = 12.dp,
            viewportWidth = 7.4f,
            viewportHeight = 12f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4.6f, 6f)
                lineTo(0f, 1.4f)
                lineTo(1.4f, 0f)
                lineTo(7.4f, 6f)
                lineTo(1.4f, 12f)
                lineTo(0f, 10.6f)
                lineTo(4.6f, 6f)
                verticalLineTo(6f)
            }
        }.build()

        return _ChevronRight!!
    }

@Suppress("ObjectPropertyName")
private var _ChevronRight: ImageVector? = null

@Preview
@Composable
private fun ChevronRightPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.ChevronRight, contentDescription = null)
    }
}
