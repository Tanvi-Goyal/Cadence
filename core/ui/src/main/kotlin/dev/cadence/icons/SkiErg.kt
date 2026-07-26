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

val CadenceIcons.SkiErg: ImageVector
    get() {
        if (_SkiErg != null) {
            return _SkiErg!!
        }
        _SkiErg = ImageVector.Builder(
            name = "SkiErg",
            defaultWidth = 17.dp,
            defaultHeight = 23.dp,
            viewportWidth = 17f,
            viewportHeight = 23f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(14f, 23f)
                lineTo(11f, 20f)
                verticalLineTo(18.5f)
                lineTo(3.9f, 11.4f)
                curveTo(3.75f, 11.433f, 3.6f, 11.458f, 3.45f, 11.475f)
                curveTo(3.3f, 11.492f, 3.15f, 11.5f, 3f, 11.5f)
                verticalLineTo(9.3f)
                curveTo(3.833f, 9.333f, 4.683f, 9.154f, 5.55f, 8.762f)
                curveTo(6.417f, 8.371f, 7.117f, 7.883f, 7.65f, 7.3f)
                lineTo(9.05f, 5.75f)
                curveTo(9.267f, 5.5f, 9.521f, 5.313f, 9.813f, 5.188f)
                curveTo(10.104f, 5.063f, 10.417f, 5f, 10.75f, 5f)
                curveTo(11.383f, 5f, 11.917f, 5.217f, 12.35f, 5.65f)
                curveTo(12.783f, 6.083f, 13f, 6.617f, 13f, 7.25f)
                verticalLineTo(13f)
                curveTo(13f, 13.433f, 12.921f, 13.829f, 12.762f, 14.188f)
                curveTo(12.604f, 14.546f, 12.383f, 14.867f, 12.1f, 15.15f)
                lineTo(8.5f, 11.6f)
                verticalLineTo(9.3f)
                curveTo(8.167f, 9.583f, 7.808f, 9.842f, 7.425f, 10.075f)
                curveTo(7.042f, 10.308f, 6.633f, 10.517f, 6.2f, 10.7f)
                lineTo(12.5f, 17f)
                horizontalLineTo(14f)
                lineTo(17f, 20f)
                lineTo(14f, 23f)
                verticalLineTo(23f)
                moveTo(1.5f, 19.5f)
                lineTo(0f, 18f)
                lineTo(4.5f, 13.5f)
                lineTo(7f, 16f)
                horizontalLineTo(5f)
                lineTo(1.5f, 19.5f)
                verticalLineTo(19.5f)
                moveTo(11f, 4f)
                curveTo(10.45f, 4f, 9.979f, 3.804f, 9.587f, 3.412f)
                curveTo(9.196f, 3.021f, 9f, 2.55f, 9f, 2f)
                curveTo(9f, 1.45f, 9.196f, 0.979f, 9.587f, 0.587f)
                curveTo(9.979f, 0.196f, 10.45f, 0f, 11f, 0f)
                curveTo(11.55f, 0f, 12.021f, 0.196f, 12.413f, 0.587f)
                curveTo(12.804f, 0.979f, 13f, 1.45f, 13f, 2f)
                curveTo(13f, 2.55f, 12.804f, 3.021f, 12.413f, 3.412f)
                curveTo(12.021f, 3.804f, 11.55f, 4f, 11f, 4f)
                verticalLineTo(4f)
            }
        }.build()

        return _SkiErg!!
    }

@Suppress("ObjectPropertyName")
private var _SkiErg: ImageVector? = null

@Preview
@Composable
private fun SkiErgPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.SkiErg, contentDescription = null)
    }
}
