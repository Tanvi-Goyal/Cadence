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

val CadenceIcons.Tune: ImageVector
    get() {
        if (_Tune != null) {
            return _Tune!!
        }
        _Tune = ImageVector.Builder(
            name = "Tune",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(8f, 18f)
                verticalLineTo(12f)
                horizontalLineTo(10f)
                verticalLineTo(14f)
                horizontalLineTo(18f)
                verticalLineTo(16f)
                horizontalLineTo(10f)
                verticalLineTo(18f)
                horizontalLineTo(8f)
                verticalLineTo(18f)
                moveTo(0f, 16f)
                verticalLineTo(14f)
                horizontalLineTo(6f)
                verticalLineTo(16f)
                horizontalLineTo(0f)
                verticalLineTo(16f)
                moveTo(4f, 12f)
                verticalLineTo(10f)
                horizontalLineTo(0f)
                verticalLineTo(8f)
                horizontalLineTo(4f)
                verticalLineTo(6f)
                horizontalLineTo(6f)
                verticalLineTo(12f)
                horizontalLineTo(4f)
                verticalLineTo(12f)
                moveTo(8f, 10f)
                verticalLineTo(8f)
                horizontalLineTo(18f)
                verticalLineTo(10f)
                horizontalLineTo(8f)
                verticalLineTo(10f)
                moveTo(12f, 6f)
                verticalLineTo(0f)
                horizontalLineTo(14f)
                verticalLineTo(2f)
                horizontalLineTo(18f)
                verticalLineTo(4f)
                horizontalLineTo(14f)
                verticalLineTo(6f)
                horizontalLineTo(12f)
                verticalLineTo(6f)
                moveTo(0f, 4f)
                verticalLineTo(2f)
                horizontalLineTo(10f)
                verticalLineTo(4f)
                horizontalLineTo(0f)
                verticalLineTo(4f)
            }
        }.build()

        return _Tune!!
    }

@Suppress("ObjectPropertyName")
private var _Tune: ImageVector? = null

@Preview
@Composable
private fun TunePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Tune, contentDescription = null)
    }
}
