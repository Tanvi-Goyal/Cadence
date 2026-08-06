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

val MindSetIcons.MoreVert: ImageVector
    get() {
        if (_MoreVert != null) {
            return _MoreVert!!
        }
        _MoreVert = ImageVector.Builder(
            name = "MoreVert",
            defaultWidth = 4.dp,
            defaultHeight = 16.dp,
            viewportWidth = 4f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(2f, 16f)
                curveTo(1.45f, 16f, 0.979f, 15.804f, 0.587f, 15.413f)
                curveTo(0.196f, 15.021f, 0f, 14.55f, 0f, 14f)
                curveTo(0f, 13.45f, 0.196f, 12.979f, 0.587f, 12.587f)
                curveTo(0.979f, 12.196f, 1.45f, 12f, 2f, 12f)
                curveTo(2.55f, 12f, 3.021f, 12.196f, 3.412f, 12.587f)
                curveTo(3.804f, 12.979f, 4f, 13.45f, 4f, 14f)
                curveTo(4f, 14.55f, 3.804f, 15.021f, 3.412f, 15.413f)
                curveTo(3.021f, 15.804f, 2.55f, 16f, 2f, 16f)
                verticalLineTo(16f)
                moveTo(2f, 10f)
                curveTo(1.45f, 10f, 0.979f, 9.804f, 0.587f, 9.413f)
                curveTo(0.196f, 9.021f, 0f, 8.55f, 0f, 8f)
                curveTo(0f, 7.45f, 0.196f, 6.979f, 0.587f, 6.588f)
                curveTo(0.979f, 6.196f, 1.45f, 6f, 2f, 6f)
                curveTo(2.55f, 6f, 3.021f, 6.196f, 3.412f, 6.588f)
                curveTo(3.804f, 6.979f, 4f, 7.45f, 4f, 8f)
                curveTo(4f, 8.55f, 3.804f, 9.021f, 3.412f, 9.413f)
                curveTo(3.021f, 9.804f, 2.55f, 10f, 2f, 10f)
                verticalLineTo(10f)
                moveTo(2f, 4f)
                curveTo(1.45f, 4f, 0.979f, 3.804f, 0.587f, 3.412f)
                curveTo(0.196f, 3.021f, 0f, 2.55f, 0f, 2f)
                curveTo(0f, 1.45f, 0.196f, 0.979f, 0.587f, 0.587f)
                curveTo(0.979f, 0.196f, 1.45f, 0f, 2f, 0f)
                curveTo(2.55f, 0f, 3.021f, 0.196f, 3.412f, 0.587f)
                curveTo(3.804f, 0.979f, 4f, 1.45f, 4f, 2f)
                curveTo(4f, 2.55f, 3.804f, 3.021f, 3.412f, 3.412f)
                curveTo(3.021f, 3.804f, 2.55f, 4f, 2f, 4f)
                verticalLineTo(4f)
            }
        }.build()

        return _MoreVert!!
    }

@Suppress("ObjectPropertyName")
private var _MoreVert: ImageVector? = null

@Preview
@Composable
private fun MoreVertPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.MoreVert, contentDescription = null)
    }
}
