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

val CadenceIcons.ArrowBack: ImageVector
    get() {
        if (_ArrowBack != null) {
            return _ArrowBack!!
        }
        _ArrowBack = ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(3.825f, 9f)
                lineTo(9.425f, 14.6f)
                lineTo(8f, 16f)
                lineTo(0f, 8f)
                lineTo(8f, 0f)
                lineTo(9.425f, 1.4f)
                lineTo(3.825f, 7f)
                horizontalLineTo(16f)
                verticalLineTo(9f)
                horizontalLineTo(3.825f)
                verticalLineTo(9f)
            }
        }.build()

        return _ArrowBack!!
    }

@Suppress("ObjectPropertyName")
private var _ArrowBack: ImageVector? = null

@Preview
@Composable
private fun ArrowBackPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.ArrowBack, contentDescription = null)
    }
}
