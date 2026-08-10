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

val MindSetIcons.LowerBody: ImageVector
    get() {
        if (_LowerBody != null) {
            return _LowerBody!!
        }
        _LowerBody = ImageVector.Builder(
            name = "LowerBody",
            defaultWidth = 18.dp,
            defaultHeight = 24.dp,
            viewportWidth = 18f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.White)) {
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
                moveTo(6f, 19f)
                verticalLineTo(7f)
                curveTo(5f, 6.917f, 3.983f, 6.792f, 2.95f, 6.625f)
                curveTo(1.917f, 6.458f, 0.933f, 6.25f, 0f, 6f)
                lineTo(0.5f, 4f)
                curveTo(1.8f, 4.35f, 3.183f, 4.604f, 4.65f, 4.762f)
                curveTo(6.117f, 4.921f, 7.567f, 5f, 9f, 5f)
                curveTo(10.433f, 5f, 11.883f, 4.921f, 13.35f, 4.762f)
                curveTo(14.817f, 4.604f, 16.2f, 4.35f, 17.5f, 4f)
                lineTo(18f, 6f)
                curveTo(17.067f, 6.25f, 16.083f, 6.458f, 15.05f, 6.625f)
                curveTo(14.017f, 6.792f, 13f, 6.917f, 12f, 7f)
                verticalLineTo(19f)
                horizontalLineTo(10f)
                verticalLineTo(13f)
                horizontalLineTo(8f)
                verticalLineTo(19f)
                horizontalLineTo(6f)
                verticalLineTo(19f)
                moveTo(5f, 24f)
                curveTo(4.717f, 24f, 4.479f, 23.904f, 4.287f, 23.712f)
                curveTo(4.096f, 23.521f, 4f, 23.283f, 4f, 23f)
                curveTo(4f, 22.717f, 4.096f, 22.479f, 4.287f, 22.288f)
                curveTo(4.479f, 22.096f, 4.717f, 22f, 5f, 22f)
                curveTo(5.283f, 22f, 5.521f, 22.096f, 5.713f, 22.288f)
                curveTo(5.904f, 22.479f, 6f, 22.717f, 6f, 23f)
                curveTo(6f, 23.283f, 5.904f, 23.521f, 5.713f, 23.712f)
                curveTo(5.521f, 23.904f, 5.283f, 24f, 5f, 24f)
                verticalLineTo(24f)
                moveTo(9f, 24f)
                curveTo(8.717f, 24f, 8.479f, 23.904f, 8.288f, 23.712f)
                curveTo(8.096f, 23.521f, 8f, 23.283f, 8f, 23f)
                curveTo(8f, 22.717f, 8.096f, 22.479f, 8.288f, 22.288f)
                curveTo(8.479f, 22.096f, 8.717f, 22f, 9f, 22f)
                curveTo(9.283f, 22f, 9.521f, 22.096f, 9.712f, 22.288f)
                curveTo(9.904f, 22.479f, 10f, 22.717f, 10f, 23f)
                curveTo(10f, 23.283f, 9.904f, 23.521f, 9.712f, 23.712f)
                curveTo(9.521f, 23.904f, 9.283f, 24f, 9f, 24f)
                verticalLineTo(24f)
                moveTo(13f, 24f)
                curveTo(12.717f, 24f, 12.479f, 23.904f, 12.288f, 23.712f)
                curveTo(12.096f, 23.521f, 12f, 23.283f, 12f, 23f)
                curveTo(12f, 22.717f, 12.096f, 22.479f, 12.288f, 22.288f)
                curveTo(12.479f, 22.096f, 12.717f, 22f, 13f, 22f)
                curveTo(13.283f, 22f, 13.521f, 22.096f, 13.712f, 22.288f)
                curveTo(13.904f, 22.479f, 14f, 22.717f, 14f, 23f)
                curveTo(14f, 23.283f, 13.904f, 23.521f, 13.712f, 23.712f)
                curveTo(13.521f, 23.904f, 13.283f, 24f, 13f, 24f)
                verticalLineTo(24f)
            }
        }.build()

        return _LowerBody!!
    }

@Suppress("ObjectPropertyName")
private var _LowerBody: ImageVector? = null

@Preview
@Composable
private fun LowerBodyPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.LowerBody, contentDescription = null)
    }
}
