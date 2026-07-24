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

val CadenceIcons.Rowing: ImageVector
    get() {
        if (_Rowing != null) {
            return _Rowing!!
        }
        _Rowing = ImageVector.Builder(
            name = "Rowing",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(14.5f, 3f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 14.5f, 9f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 14.5f, 3f)
                close()
                moveTo(12f, 9.5f)
                lineTo(3.5f, 18f)
                lineTo(2f, 21f)
                lineTo(2f, 22f)
                lineTo(3f, 22f)
                lineTo(6f, 20.5f)
                lineTo(14.5f, 12f)
                lineTo(12f, 9.5f)
                close()
            }
        }.build()

        return _Rowing!!
    }

@Suppress("ObjectPropertyName")
private var _Rowing: ImageVector? = null

@Preview
@Composable
private fun RowingPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Rowing, contentDescription = null)
    }
}
