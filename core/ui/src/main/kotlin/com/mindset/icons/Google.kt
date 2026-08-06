package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/*
 * Official multi-color Google "G" mark (4 brand-colored paths). Render UNTINTED — e.g.
 * Icon(tint = Color.Unspecified) or Image(...) — so the brand colors are preserved.
 */
val MindSetIcons.Google: ImageVector
    get() {
        if (_Google != null) return _Google!!
        _Google = ImageVector.Builder(
            name = "Google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 48f,
            viewportHeight = 48f,
        )
            .addPath(
                // Blue
                pathData = PathParser().parsePathString(
                    "M45.12,24.5c0,-1.56 -0.14,-3.06 -0.4,-4.5H24v8.51h11.84c-0.51,2.75 -2.06,5.08 " +
                        "-4.39,6.64v5.52h7.11C42.72,36.86 45.12,31.14 45.12,24.5z",
                ).toNodes(),
                fill = SolidColor(Color(0xFF4285F4)),
            )
            .addPath(
                // Green
                pathData = PathParser().parsePathString(
                    "M24,46c5.94,0 10.92,-1.97 14.56,-5.33l-7.11,-5.52c-1.97,1.32 -4.49,2.1 -7.45,2.1 " +
                        "-5.73,0 -10.58,-3.87 -12.31,-9.07H4.34v5.7C7.96,41.07 15.4,46 24,46z",
                ).toNodes(),
                fill = SolidColor(Color(0xFF34A853)),
            )
            .addPath(
                // Yellow
                pathData = PathParser().parsePathString(
                    "M11.69,28.18C11.25,26.86 11,25.45 11,24s0.25,-2.86 0.69,-4.18v-5.7H4.34C2.85,17.09 " +
                        "2,20.45 2,24s0.85,6.91 2.34,9.88l7.35,-5.7z",
                ).toNodes(),
                fill = SolidColor(Color(0xFFFBBC05)),
            )
            .addPath(
                // Red
                pathData = PathParser().parsePathString(
                    "M24,10.75c3.23,0 6.13,1.11 8.41,3.29l6.31,-6.31C34.91,4.18 29.93,2 24,2 15.4,2 " +
                        "7.96,6.93 4.34,14.12l7.35,5.7c1.73,-5.2 6.58,-9.07 12.31,-9.07z",
                ).toNodes(),
                fill = SolidColor(Color(0xFFEA4335)),
            )
            .build()
        return _Google!!
    }

@Suppress("ObjectPropertyName")
private var _Google: ImageVector? = null
