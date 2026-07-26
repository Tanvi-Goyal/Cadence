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

val CadenceIcons.Bolt: ImageVector
    get() {
        if (_Bolt != null) {
            return _Bolt!!
        }
        _Bolt = ImageVector.Builder(
            name = "Bolt",
            defaultWidth = 13.333.dp,
            defaultHeight = 16.667.dp,
            viewportWidth = 13.333f,
            viewportHeight = 16.667f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(3.333f, 16.667f)
                lineTo(4.167f, 10.833f)
                horizontalLineTo(0f)
                lineTo(7.5f, 0f)
                horizontalLineTo(9.167f)
                lineTo(8.333f, 6.667f)
                horizontalLineTo(13.333f)
                lineTo(5f, 16.667f)
                horizontalLineTo(3.333f)
                verticalLineTo(16.667f)
            }
        }.build()

        return _Bolt!!
    }

@Suppress("ObjectPropertyName")
private var _Bolt: ImageVector? = null

@Preview
@Composable
private fun BoltPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Bolt, contentDescription = null)
    }
}
