package com.mindset.icons

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
import com.mindset.MindSetIcons

val MindSetIcons.Share: ImageVector
    get() {
        if (_Share != null) {
            return _Share!!
        }
        _Share = ImageVector.Builder(
            name = "Share",
            defaultWidth = 18.dp,
            defaultHeight = 20.dp,
            viewportWidth = 18f,
            viewportHeight = 20f,
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(15f, 20f)
                curveTo(14.167f, 20f, 13.458f, 19.708f, 12.875f, 19.125f)
                curveTo(12.292f, 18.542f, 12f, 17.833f, 12f, 17f)
                curveTo(12f, 16.9f, 12.025f, 16.667f, 12.075f, 16.3f)
                lineTo(5.05f, 12.2f)
                curveTo(4.783f, 12.45f, 4.475f, 12.646f, 4.125f, 12.788f)
                curveTo(3.775f, 12.929f, 3.4f, 13f, 3f, 13f)
                curveTo(2.167f, 13f, 1.458f, 12.708f, 0.875f, 12.125f)
                curveTo(0.292f, 11.542f, 0f, 10.833f, 0f, 10f)
                curveTo(0f, 9.167f, 0.292f, 8.458f, 0.875f, 7.875f)
                curveTo(1.458f, 7.292f, 2.167f, 7f, 3f, 7f)
                curveTo(3.4f, 7f, 3.775f, 7.071f, 4.125f, 7.213f)
                curveTo(4.475f, 7.354f, 4.783f, 7.55f, 5.05f, 7.8f)
                lineTo(12.075f, 3.7f)
                curveTo(12.042f, 3.583f, 12.021f, 3.471f, 12.012f, 3.362f)
                curveTo(12.004f, 3.254f, 12f, 3.133f, 12f, 3f)
                curveTo(12f, 2.167f, 12.292f, 1.458f, 12.875f, 0.875f)
                curveTo(13.458f, 0.292f, 14.167f, 0f, 15f, 0f)
                curveTo(15.833f, 0f, 16.542f, 0.292f, 17.125f, 0.875f)
                curveTo(17.708f, 1.458f, 18f, 2.167f, 18f, 3f)
                curveTo(18f, 3.833f, 17.708f, 4.542f, 17.125f, 5.125f)
                curveTo(16.542f, 5.708f, 15.833f, 6f, 15f, 6f)
                curveTo(14.6f, 6f, 14.225f, 5.929f, 13.875f, 5.787f)
                curveTo(13.525f, 5.646f, 13.217f, 5.45f, 12.95f, 5.2f)
                lineTo(5.925f, 9.3f)
                curveTo(5.958f, 9.417f, 5.979f, 9.529f, 5.988f, 9.637f)
                curveTo(5.996f, 9.746f, 6f, 9.867f, 6f, 10f)
                curveTo(6f, 10.133f, 5.996f, 10.254f, 5.988f, 10.363f)
                curveTo(5.979f, 10.471f, 5.958f, 10.583f, 5.925f, 10.7f)
                lineTo(12.95f, 14.8f)
                curveTo(13.217f, 14.55f, 13.525f, 14.354f, 13.875f, 14.212f)
                curveTo(14.225f, 14.071f, 14.6f, 14f, 15f, 14f)
                curveTo(15.833f, 14f, 16.542f, 14.292f, 17.125f, 14.875f)
                curveTo(17.708f, 15.458f, 18f, 16.167f, 18f, 17f)
                curveTo(18f, 17.833f, 17.708f, 18.542f, 17.125f, 19.125f)
                curveTo(16.542f, 19.708f, 15.833f, 20f, 15f, 20f)
                verticalLineTo(20f)
                moveTo(15f, 18f)
                curveTo(15.283f, 18f, 15.521f, 17.904f, 15.712f, 17.712f)
                curveTo(15.904f, 17.521f, 16f, 17.283f, 16f, 17f)
                curveTo(16f, 16.717f, 15.904f, 16.479f, 15.712f, 16.288f)
                curveTo(15.521f, 16.096f, 15.283f, 16f, 15f, 16f)
                curveTo(14.717f, 16f, 14.479f, 16.096f, 14.288f, 16.288f)
                curveTo(14.096f, 16.479f, 14f, 16.717f, 14f, 17f)
                curveTo(14f, 17.283f, 14.096f, 17.521f, 14.288f, 17.712f)
                curveTo(14.479f, 17.904f, 14.717f, 18f, 15f, 18f)
                verticalLineTo(18f)
                moveTo(3f, 11f)
                curveTo(3.283f, 11f, 3.521f, 10.904f, 3.713f, 10.712f)
                curveTo(3.904f, 10.521f, 4f, 10.283f, 4f, 10f)
                curveTo(4f, 9.717f, 3.904f, 9.479f, 3.713f, 9.288f)
                curveTo(3.521f, 9.096f, 3.283f, 9f, 3f, 9f)
                curveTo(2.717f, 9f, 2.479f, 9.096f, 2.287f, 9.288f)
                curveTo(2.096f, 9.479f, 2f, 9.717f, 2f, 10f)
                curveTo(2f, 10.283f, 2.096f, 10.521f, 2.287f, 10.712f)
                curveTo(2.479f, 10.904f, 2.717f, 11f, 3f, 11f)
                verticalLineTo(11f)
                moveTo(15f, 4f)
                curveTo(15.283f, 4f, 15.521f, 3.904f, 15.712f, 3.713f)
                curveTo(15.904f, 3.521f, 16f, 3.283f, 16f, 3f)
                curveTo(16f, 2.717f, 15.904f, 2.479f, 15.712f, 2.287f)
                curveTo(15.521f, 2.096f, 15.283f, 2f, 15f, 2f)
                curveTo(14.717f, 2f, 14.479f, 2.096f, 14.288f, 2.287f)
                curveTo(14.096f, 2.479f, 14f, 2.717f, 14f, 3f)
                curveTo(14f, 3.283f, 14.096f, 3.521f, 14.288f, 3.713f)
                curveTo(14.479f, 3.904f, 14.717f, 4f, 15f, 4f)
                verticalLineTo(4f)
            }
        }.build()

        return _Share!!
    }

@Suppress("ObjectPropertyName")
private var _Share: ImageVector? = null

@Preview
@Composable
private fun SharePreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.Share, contentDescription = null)
    }
}
