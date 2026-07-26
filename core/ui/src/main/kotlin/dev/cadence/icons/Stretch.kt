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

val CadenceIcons.Stretch: ImageVector
    get() {
        if (_Stretch != null) {
            return _Stretch!!
        }
        _Stretch = ImageVector.Builder(
            name = "Stretch",
            defaultWidth = 18.dp,
            defaultHeight = 16.dp,
            viewportWidth = 18f,
            viewportHeight = 16f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(3.8f, 16f)
                curveTo(3.3f, 16f, 2.875f, 15.825f, 2.525f, 15.475f)
                curveTo(2.175f, 15.125f, 2f, 14.7f, 2f, 14.2f)
                curveTo(2f, 13.85f, 2.1f, 13.521f, 2.3f, 13.212f)
                curveTo(2.5f, 12.904f, 2.767f, 12.683f, 3.1f, 12.55f)
                lineTo(7f, 11f)
                verticalLineTo(8.75f)
                curveTo(6.1f, 9.8f, 5.054f, 10.604f, 3.862f, 11.163f)
                curveTo(2.671f, 11.721f, 1.383f, 12f, 0f, 12f)
                verticalLineTo(10f)
                curveTo(1.133f, 10f, 2.162f, 9.767f, 3.088f, 9.3f)
                curveTo(4.012f, 8.833f, 4.85f, 8.167f, 5.6f, 7.3f)
                lineTo(6.95f, 5.7f)
                curveTo(7.15f, 5.467f, 7.383f, 5.292f, 7.65f, 5.175f)
                curveTo(7.917f, 5.058f, 8.2f, 5f, 8.5f, 5f)
                horizontalLineTo(9.5f)
                curveTo(9.8f, 5f, 10.083f, 5.058f, 10.35f, 5.175f)
                curveTo(10.617f, 5.292f, 10.85f, 5.467f, 11.05f, 5.7f)
                lineTo(12.4f, 7.3f)
                curveTo(13.15f, 8.167f, 13.988f, 8.833f, 14.913f, 9.3f)
                curveTo(15.837f, 9.767f, 16.867f, 10f, 18f, 10f)
                verticalLineTo(12f)
                curveTo(16.617f, 12f, 15.329f, 11.721f, 14.137f, 11.163f)
                curveTo(12.946f, 10.604f, 11.9f, 9.8f, 11f, 8.75f)
                verticalLineTo(11f)
                lineTo(14.9f, 12.55f)
                curveTo(15.233f, 12.683f, 15.5f, 12.904f, 15.7f, 13.212f)
                curveTo(15.9f, 13.521f, 16f, 13.85f, 16f, 14.2f)
                curveTo(16f, 14.7f, 15.825f, 15.125f, 15.475f, 15.475f)
                curveTo(15.125f, 15.825f, 14.7f, 16f, 14.2f, 16f)
                horizontalLineTo(7f)
                verticalLineTo(15.5f)
                curveTo(7f, 15.067f, 7.142f, 14.708f, 7.425f, 14.425f)
                curveTo(7.708f, 14.142f, 8.067f, 14f, 8.5f, 14f)
                horizontalLineTo(11.5f)
                curveTo(11.65f, 14f, 11.771f, 13.954f, 11.863f, 13.863f)
                curveTo(11.954f, 13.771f, 12f, 13.65f, 12f, 13.5f)
                curveTo(12f, 13.35f, 11.954f, 13.229f, 11.863f, 13.137f)
                curveTo(11.771f, 13.046f, 11.65f, 13f, 11.5f, 13f)
                horizontalLineTo(8.5f)
                curveTo(7.8f, 13f, 7.208f, 13.242f, 6.725f, 13.725f)
                curveTo(6.242f, 14.208f, 6f, 14.8f, 6f, 15.5f)
                verticalLineTo(16f)
                horizontalLineTo(3.8f)
                verticalLineTo(16f)
                moveTo(9f, 4f)
                curveTo(8.45f, 4f, 7.979f, 3.804f, 7.588f, 3.412f)
                curveTo(7.196f, 3.021f, 7f, 2.55f, 7f, 2f)
                curveTo(7f, 1.45f, 7.196f, 0.979f, 7.588f, 0.587f)
                curveTo(7.979f, 0.196f, 8.45f, 0f, 9f, 0f)
                curveTo(9.55f, 0f, 10.021f, 0.196f, 10.413f, 0.587f)
                curveTo(10.804f, 0.979f, 11f, 1.45f, 11f, 2f)
                curveTo(11f, 2.55f, 10.804f, 3.021f, 10.413f, 3.412f)
                curveTo(10.021f, 3.804f, 9.55f, 4f, 9f, 4f)
                verticalLineTo(4f)
            }
        }.build()

        return _Stretch!!
    }

@Suppress("ObjectPropertyName")
private var _Stretch: ImageVector? = null

@Preview
@Composable
private fun StretchPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.Stretch, contentDescription = null)
    }
}
