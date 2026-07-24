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

val CadenceIcons.Burpee: ImageVector
    get() {
        if (_Burpee != null) {
            return _Burpee!!
        }
        _Burpee = ImageVector.Builder(
            name = "Burpee",
            defaultWidth = 21.95.dp,
            defaultHeight = 21.95.dp,
            viewportWidth = 21.95f,
            viewportHeight = 21.95f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 15.95f)
                lineTo(9.45f, 12.5f)
                lineTo(6f, 10.95f)
                lineTo(9.45f, 9.375f)
                lineTo(11f, 5.95f)
                lineTo(12.575f, 9.375f)
                lineTo(16f, 10.95f)
                lineTo(12.575f, 12.5f)
                lineTo(11f, 15.95f)
                moveTo(11f, 21.95f)
                curveTo(9.2f, 21.95f, 7.512f, 21.538f, 5.938f, 20.712f)
                curveTo(4.363f, 19.888f, 3.05f, 18.733f, 2f, 17.25f)
                verticalLineTo(19.95f)
                horizontalLineTo(0f)
                verticalLineTo(13.95f)
                horizontalLineTo(6f)
                verticalLineTo(15.95f)
                horizontalLineTo(3.55f)
                curveTo(4.4f, 17.2f, 5.479f, 18.179f, 6.787f, 18.888f)
                curveTo(8.096f, 19.596f, 9.5f, 19.95f, 11f, 19.95f)
                curveTo(12.917f, 19.95f, 14.654f, 19.4f, 16.212f, 18.3f)
                curveTo(17.771f, 17.2f, 18.867f, 15.742f, 19.5f, 13.925f)
                lineTo(21.45f, 14.375f)
                curveTo(20.7f, 16.642f, 19.367f, 18.471f, 17.45f, 19.862f)
                curveTo(15.533f, 21.254f, 13.383f, 21.95f, 11f, 21.95f)
                moveTo(0.05f, 9.95f)
                curveTo(0.167f, 8.833f, 0.433f, 7.762f, 0.85f, 6.738f)
                curveTo(1.267f, 5.713f, 1.842f, 4.767f, 2.575f, 3.9f)
                lineTo(4f, 5.325f)
                curveTo(3.467f, 6.008f, 3.033f, 6.738f, 2.7f, 7.512f)
                curveTo(2.367f, 8.288f, 2.158f, 9.1f, 2.075f, 9.95f)
                horizontalLineTo(0.05f)
                moveTo(5.4f, 3.925f)
                lineTo(3.975f, 2.5f)
                curveTo(4.858f, 1.767f, 5.808f, 1.188f, 6.825f, 0.762f)
                curveTo(7.842f, 0.338f, 8.9f, 0.083f, 10f, 0f)
                verticalLineTo(2f)
                curveTo(9.15f, 2.083f, 8.342f, 2.292f, 7.575f, 2.625f)
                curveTo(6.808f, 2.958f, 6.083f, 3.392f, 5.4f, 3.925f)
                moveTo(16.625f, 3.925f)
                curveTo(15.942f, 3.392f, 15.212f, 2.958f, 14.438f, 2.625f)
                curveTo(13.663f, 2.292f, 12.85f, 2.083f, 12f, 2f)
                verticalLineTo(0f)
                curveTo(13.117f, 0.1f, 14.188f, 0.358f, 15.212f, 0.775f)
                curveTo(16.237f, 1.192f, 17.183f, 1.767f, 18.05f, 2.5f)
                lineTo(16.625f, 3.925f)
                moveTo(19.95f, 9.95f)
                curveTo(19.867f, 9.1f, 19.658f, 8.288f, 19.325f, 7.512f)
                curveTo(18.992f, 6.738f, 18.558f, 6.008f, 18.025f, 5.325f)
                lineTo(19.45f, 3.9f)
                curveTo(20.183f, 4.767f, 20.758f, 5.713f, 21.175f, 6.738f)
                curveTo(21.592f, 7.762f, 21.85f, 8.833f, 21.95f, 9.95f)
                horizontalLineTo(19.95f)
            }
        }.build()

        return _Burpee!!
    }

@Suppress("ObjectPropertyName")
private var _Burpee: ImageVector? = null

@Preview
@Composable
private fun BurpeePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Burpee, contentDescription = null)
    }
}
