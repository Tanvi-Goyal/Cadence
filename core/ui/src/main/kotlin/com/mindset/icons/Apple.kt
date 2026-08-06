package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Apple logo silhouette (monochrome). Tinted at the call site (white on the dark button). */
val MindSetIcons.Apple: ImageVector
    get() {
        if (_Apple != null) return _Apple!!
        _Apple = ImageVector.Builder(
            name = "Apple",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M17.05,20.28c-0.98,0.95 -2.05,0.8 -3.08,0.35 -1.09,-0.46 -2.09,-0.48 -3.24,0 " +
                    "-1.44,0.62 -2.2,0.44 -3.06,-0.35C2.79,15.25 3.51,7.59 9.05,7.31c1.35,0.07 " +
                    "2.29,0.74 3.08,0.8 1.18,-0.24 2.31,-0.93 3.57,-0.84 1.51,0.12 2.65,0.72 3.4,1.8 " +
                    "-3.12,1.87 -2.38,5.98 0.48,7.13 -0.57,1.5 -1.31,2.99 -2.54,4.09l0.01,-0.01zM12.03,7.25c" +
                    "-0.15,-2.23 1.66,-4.07 3.74,-4.25 0.29,2.58 -2.34,4.5 -3.74,4.25z",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Apple!!
    }

@Suppress("ObjectPropertyName")
private var _Apple: ImageVector? = null
