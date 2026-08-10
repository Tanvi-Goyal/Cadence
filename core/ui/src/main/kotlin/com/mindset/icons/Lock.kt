package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

val MindSetIcons.Lock: ImageVector
    get() {
        if (_Lock != null) {
            return _Lock!!
        }
        _Lock =
            ImageVector
                .Builder(
                    name = "Lock",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(18f, 8f)
                        horizontalLineToRelative(-1f)
                        verticalLineTo(6f)
                        curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
                        reflectiveCurveTo(7f, 3.24f, 7f, 6f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(6f)
                        curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                        verticalLineToRelative(10f)
                        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                        horizontalLineToRelative(12f)
                        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                        verticalLineTo(10f)
                        curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                        close()
                        moveToRelative(-6f, 9f)
                        curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
                        reflectiveCurveToRelative(0.9f, -2f, 2f, -2f)
                        reflectiveCurveToRelative(2f, 0.9f, 2f, 2f)
                        reflectiveCurveToRelative(-0.9f, 2f, -2f, 2f)
                        close()
                        moveToRelative(3.1f, -9f)
                        horizontalLineTo(8.9f)
                        verticalLineTo(6f)
                        curveToRelative(0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                        curveToRelative(1.71f, 0f, 3.1f, 1.39f, 3.1f, 3.1f)
                        verticalLineToRelative(2f)
                        close()
                    }
                }.build()

        return _Lock!!
    }

@Suppress("ObjectPropertyName")
private var _Lock: ImageVector? = null
