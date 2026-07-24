package dev.cadence.icons

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
import dev.cadence.CadenceIcons

val CadenceIcons.Edit: ImageVector
    get() {
        if (_Edit != null) {
            return _Edit!!
        }
        _Edit = ImageVector.Builder(
            name = "Edit",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(2f, 16f)
                horizontalLineTo(3.425f)
                lineTo(13.2f, 6.225f)
                lineTo(11.775f, 4.8f)
                lineTo(2f, 14.575f)
                verticalLineTo(16f)
                moveTo(0f, 18f)
                verticalLineTo(13.75f)
                lineTo(13.2f, 0.575f)
                curveTo(13.4f, 0.392f, 13.621f, 0.25f, 13.863f, 0.15f)
                curveTo(14.104f, 0.05f, 14.358f, 0f, 14.625f, 0f)
                curveTo(14.892f, 0f, 15.15f, 0.05f, 15.4f, 0.15f)
                curveTo(15.65f, 0.25f, 15.867f, 0.4f, 16.05f, 0.6f)
                lineTo(17.425f, 2f)
                curveTo(17.625f, 2.183f, 17.771f, 2.4f, 17.862f, 2.65f)
                curveTo(17.954f, 2.9f, 18f, 3.15f, 18f, 3.4f)
                curveTo(18f, 3.667f, 17.954f, 3.921f, 17.862f, 4.162f)
                curveTo(17.771f, 4.404f, 17.625f, 4.625f, 17.425f, 4.825f)
                lineTo(4.25f, 18f)
                horizontalLineTo(0f)
                moveTo(16f, 3.4f)
                lineTo(14.6f, 2f)
                lineTo(16f, 3.4f)
                moveTo(12.475f, 5.525f)
                lineTo(11.775f, 4.8f)
                lineTo(13.2f, 6.225f)
                lineTo(12.475f, 5.525f)
            }
        }.build()

        return _Edit!!
    }

@Suppress("ObjectPropertyName")
private var _Edit: ImageVector? = null

@Preview
@Composable
private fun EditPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Edit, contentDescription = null)
    }
}
