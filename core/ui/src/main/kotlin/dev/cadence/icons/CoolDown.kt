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

val CadenceIcons.CoolDown: ImageVector
    get() {
        if (_CoolDown != null) {
            return _CoolDown!!
        }
        _CoolDown = ImageVector.Builder(
            name = "CoolDown",
            defaultWidth = 20.dp,
            defaultHeight = 18.dp,
            viewportWidth = 20f,
            viewportHeight = 18f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(10f, 18f)
                curveTo(9.7f, 18f, 9.413f, 17.946f, 9.137f, 17.837f)
                curveTo(8.863f, 17.729f, 8.617f, 17.567f, 8.4f, 17.35f)
                lineTo(2.075f, 11f)
                horizontalLineTo(4.9f)
                lineTo(9.825f, 15.925f)
                curveTo(9.858f, 15.958f, 9.887f, 15.979f, 9.913f, 15.988f)
                curveTo(9.938f, 15.996f, 9.967f, 16f, 10f, 16f)
                curveTo(10.033f, 16f, 10.063f, 15.996f, 10.087f, 15.988f)
                curveTo(10.113f, 15.979f, 10.142f, 15.958f, 10.175f, 15.925f)
                lineTo(16.85f, 9.225f)
                curveTo(17.233f, 8.842f, 17.525f, 8.387f, 17.725f, 7.863f)
                curveTo(17.925f, 7.338f, 18.025f, 6.8f, 18.025f, 6.25f)
                curveTo(17.992f, 5.1f, 17.608f, 4.113f, 16.875f, 3.287f)
                curveTo(16.142f, 2.463f, 15.225f, 2.05f, 14.125f, 2.05f)
                curveTo(13.608f, 2.05f, 13.113f, 2.15f, 12.637f, 2.35f)
                curveTo(12.163f, 2.55f, 11.75f, 2.842f, 11.4f, 3.225f)
                lineTo(10.725f, 3.95f)
                curveTo(10.642f, 4.05f, 10.533f, 4.129f, 10.4f, 4.188f)
                curveTo(10.267f, 4.246f, 10.133f, 4.275f, 10f, 4.275f)
                curveTo(9.867f, 4.275f, 9.733f, 4.246f, 9.6f, 4.188f)
                curveTo(9.467f, 4.129f, 9.35f, 4.05f, 9.25f, 3.95f)
                lineTo(8.575f, 3.225f)
                curveTo(8.225f, 2.842f, 7.817f, 2.542f, 7.35f, 2.325f)
                curveTo(6.883f, 2.108f, 6.383f, 2f, 5.85f, 2f)
                curveTo(4.95f, 2f, 4.171f, 2.287f, 3.513f, 2.862f)
                curveTo(2.854f, 3.438f, 2.408f, 4.15f, 2.175f, 5f)
                horizontalLineTo(0.125f)
                curveTo(0.408f, 3.583f, 1.071f, 2.396f, 2.112f, 1.438f)
                curveTo(3.154f, 0.479f, 4.4f, 0f, 5.85f, 0f)
                curveTo(6.65f, 0f, 7.404f, 0.158f, 8.113f, 0.475f)
                curveTo(8.821f, 0.792f, 9.45f, 1.233f, 10f, 1.8f)
                curveTo(10.533f, 1.233f, 11.154f, 0.792f, 11.863f, 0.475f)
                curveTo(12.571f, 0.158f, 13.325f, 0f, 14.125f, 0f)
                curveTo(15.792f, 0f, 17.188f, 0.617f, 18.313f, 1.85f)
                curveTo(19.438f, 3.083f, 20f, 4.55f, 20f, 6.25f)
                curveTo(20f, 7.067f, 19.858f, 7.85f, 19.575f, 8.6f)
                curveTo(19.292f, 9.35f, 18.867f, 10.017f, 18.3f, 10.6f)
                lineTo(11.575f, 17.35f)
                curveTo(11.358f, 17.567f, 11.117f, 17.729f, 10.85f, 17.837f)
                curveTo(10.583f, 17.946f, 10.3f, 18f, 10f, 18f)
                verticalLineTo(18f)
                moveTo(9.875f, 9f)
                horizontalLineTo(0f)
                verticalLineTo(7f)
                horizontalLineTo(9.875f)
                horizontalLineTo(13.875f)
                curveTo(14.158f, 7f, 14.396f, 6.904f, 14.587f, 6.713f)
                curveTo(14.779f, 6.521f, 14.875f, 6.283f, 14.875f, 6f)
                curveTo(14.875f, 5.717f, 14.779f, 5.479f, 14.587f, 5.287f)
                curveTo(14.396f, 5.096f, 14.158f, 5f, 13.875f, 5f)
                curveTo(13.642f, 5f, 13.433f, 5.063f, 13.25f, 5.188f)
                curveTo(13.067f, 5.313f, 12.95f, 5.492f, 12.9f, 5.725f)
                lineTo(10.975f, 5.2f)
                curveTo(11.158f, 4.55f, 11.517f, 4.021f, 12.05f, 3.612f)
                curveTo(12.583f, 3.204f, 13.192f, 3f, 13.875f, 3f)
                curveTo(14.708f, 3f, 15.417f, 3.292f, 16f, 3.875f)
                curveTo(16.583f, 4.458f, 16.875f, 5.167f, 16.875f, 6f)
                curveTo(16.875f, 6.833f, 16.583f, 7.542f, 16f, 8.125f)
                curveTo(15.417f, 8.708f, 14.708f, 9f, 13.875f, 9f)
                horizontalLineTo(12.7f)
                curveTo(12.75f, 9.167f, 12.792f, 9.329f, 12.825f, 9.488f)
                curveTo(12.858f, 9.646f, 12.875f, 9.817f, 12.875f, 10f)
                curveTo(12.875f, 10.833f, 12.583f, 11.542f, 12f, 12.125f)
                curveTo(11.417f, 12.708f, 10.708f, 13f, 9.875f, 13f)
                curveTo(9.192f, 13f, 8.583f, 12.796f, 8.05f, 12.387f)
                curveTo(7.517f, 11.979f, 7.158f, 11.45f, 6.975f, 10.8f)
                lineTo(8.9f, 10.275f)
                curveTo(8.95f, 10.508f, 9.067f, 10.688f, 9.25f, 10.813f)
                curveTo(9.433f, 10.938f, 9.642f, 11f, 9.875f, 11f)
                curveTo(10.158f, 11f, 10.396f, 10.904f, 10.587f, 10.712f)
                curveTo(10.779f, 10.521f, 10.875f, 10.283f, 10.875f, 10f)
                curveTo(10.875f, 9.717f, 10.779f, 9.479f, 10.587f, 9.288f)
                curveTo(10.396f, 9.096f, 10.158f, 9f, 9.875f, 9f)
                verticalLineTo(9f)
            }
        }.build()

        return _CoolDown!!
    }

@Suppress("ObjectPropertyName")
private var _CoolDown: ImageVector? = null

@Preview
@Composable
private fun CoolDownPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = CadenceIcons.CoolDown, contentDescription = null)
    }
}
