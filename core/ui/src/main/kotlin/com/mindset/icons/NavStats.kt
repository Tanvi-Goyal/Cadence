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

val NavStats: ImageVector
    get() {
        if (_NavStats != null) {
            return _NavStats!!
        }
        _NavStats = ImageVector.Builder(
            name = "NavStats",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF1F1F1F))) {
                moveTo(120f, 840f)
                verticalLineToRelative(-80f)
                lineToRelative(80f, -80f)
                verticalLineToRelative(160f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(280f, 840f)
                verticalLineToRelative(-240f)
                lineToRelative(80f, -80f)
                verticalLineToRelative(320f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(440f, 840f)
                verticalLineToRelative(-320f)
                lineToRelative(80f, 81f)
                verticalLineToRelative(239f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(600f, 840f)
                verticalLineToRelative(-239f)
                lineToRelative(80f, -80f)
                verticalLineToRelative(319f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(760f, 840f)
                verticalLineToRelative(-400f)
                lineToRelative(80f, -80f)
                verticalLineToRelative(480f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(120f, 633f)
                verticalLineToRelative(-113f)
                lineToRelative(280f, -280f)
                lineToRelative(160f, 160f)
                lineToRelative(280f, -280f)
                verticalLineToRelative(113f)
                lineTo(560f, 513f)
                lineTo(400f, 353f)
                lineTo(120f, 633f)
                close()
            }
        }.build()

        return _NavStats!!
    }

@Suppress("ObjectPropertyName")
private var _NavStats: ImageVector? = null

@Preview
@Composable
private fun NavStatsPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = NavStats, contentDescription = null)
    }
}
