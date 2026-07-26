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

val CadenceIcons.Dumbbell: ImageVector
    get() {
        if (_Dumbbell != null) {
            return _Dumbbell!!
        }
        _Dumbbell = ImageVector.Builder(
            name = "Dumbbell",
            defaultWidth = 19.8.dp,
            defaultHeight = 19.8.dp,
            viewportWidth = 19.8f,
            viewportHeight = 19.8f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(11.3f, 19.8f)
                lineTo(9.9f, 18.4f)
                lineTo(13.45f, 14.85f)
                lineTo(4.95f, 6.35f)
                lineTo(1.4f, 9.9f)
                lineTo(0f, 8.5f)
                lineTo(1.4f, 7.05f)
                lineTo(0f, 5.65f)
                lineTo(2.1f, 3.55f)
                lineTo(0.7f, 2.1f)
                lineTo(2.1f, 0.7f)
                lineTo(3.55f, 2.1f)
                lineTo(5.65f, 0f)
                lineTo(7.05f, 1.4f)
                lineTo(8.5f, 0f)
                lineTo(9.9f, 1.4f)
                lineTo(6.35f, 4.95f)
                lineTo(14.85f, 13.45f)
                lineTo(18.4f, 9.9f)
                lineTo(19.8f, 11.3f)
                lineTo(18.4f, 12.75f)
                lineTo(19.8f, 14.15f)
                lineTo(17.7f, 16.25f)
                lineTo(19.1f, 17.7f)
                lineTo(17.7f, 19.1f)
                lineTo(16.25f, 17.7f)
                lineTo(14.15f, 19.8f)
                lineTo(12.75f, 18.4f)
                lineTo(11.3f, 19.8f)
                verticalLineTo(19.8f)
            }
        }.build()

        return _Dumbbell!!
    }

@Suppress("ObjectPropertyName")
private var _Dumbbell: ImageVector? = null

@Preview
@Composable
private fun DumbbellPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Dumbbell, contentDescription = null)
    }
}
