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

val CadenceIcons.EngineRun: ImageVector
    get() {
        if (_EngineRun != null) {
            return _EngineRun!!
        }
        _EngineRun = ImageVector.Builder(
            name = "EngineRun",
            defaultWidth = 32.dp,
            defaultHeight = 39.13.dp,
            viewportWidth = 32f,
            viewportHeight = 39.13f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(18f, 31.12f)
                verticalLineTo(25.12f)
                lineTo(15.9f, 23.12f)
                lineTo(14.9f, 27.52f)
                lineTo(8f, 26.12f)
                lineTo(8.4f, 24.12f)
                lineTo(13.2f, 25.12f)
                lineTo(14.8f, 17.02f)
                lineTo(13f, 17.72f)
                verticalLineTo(21.12f)
                horizontalLineTo(11f)
                verticalLineTo(16.42f)
                lineTo(14.95f, 14.72f)
                curveTo(15.533f, 14.47f, 15.962f, 14.307f, 16.237f, 14.233f)
                curveTo(16.513f, 14.158f, 16.767f, 14.12f, 17f, 14.12f)
                curveTo(17.35f, 14.12f, 17.675f, 14.212f, 17.975f, 14.395f)
                curveTo(18.275f, 14.578f, 18.517f, 14.82f, 18.7f, 15.12f)
                lineTo(19.7f, 16.72f)
                curveTo(20.133f, 17.42f, 20.721f, 17.995f, 21.462f, 18.445f)
                curveTo(22.204f, 18.895f, 23.05f, 19.12f, 24f, 19.12f)
                verticalLineTo(21.12f)
                curveTo(22.9f, 21.12f, 21.871f, 20.891f, 20.913f, 20.433f)
                curveTo(19.954f, 19.974f, 19.15f, 19.37f, 18.5f, 18.62f)
                lineTo(17.9f, 21.62f)
                lineTo(20f, 23.62f)
                verticalLineTo(31.12f)
                horizontalLineTo(18f)
                verticalLineTo(31.12f)
                moveTo(18.5f, 13.62f)
                curveTo(17.95f, 13.62f, 17.479f, 13.424f, 17.087f, 13.033f)
                curveTo(16.696f, 12.641f, 16.5f, 12.17f, 16.5f, 11.62f)
                curveTo(16.5f, 11.07f, 16.696f, 10.599f, 17.087f, 10.208f)
                curveTo(17.479f, 9.816f, 17.95f, 9.62f, 18.5f, 9.62f)
                curveTo(19.05f, 9.62f, 19.521f, 9.816f, 19.913f, 10.208f)
                curveTo(20.304f, 10.599f, 20.5f, 11.07f, 20.5f, 11.62f)
                curveTo(20.5f, 12.17f, 20.304f, 12.641f, 19.913f, 13.033f)
                curveTo(19.521f, 13.424f, 19.05f, 13.62f, 18.5f, 13.62f)
                verticalLineTo(13.62f)
            }
        }.build()

        return _EngineRun!!
    }

@Suppress("ObjectPropertyName")
private var _EngineRun: ImageVector? = null

@Preview
@Composable
private fun EngineRunPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.EngineRun, contentDescription = null)
    }
}
