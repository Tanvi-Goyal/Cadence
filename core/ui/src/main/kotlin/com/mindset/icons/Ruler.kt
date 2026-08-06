package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Straighten / ruler glyph for the "Units" preference. */
val MindSetIcons.Ruler: ImageVector
    get() {
        if (_Ruler != null) return _Ruler!!
        _Ruler = ImageVector.Builder(
            name = "Ruler",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M21,6L3,6c-1.1,0 -2,0.9 -2,2v8c0,1.1 0.9,2 2,2h18c1.1,0 2,-0.9 2,-2L23,8c0,-1.1 " +
                    "-0.9,-2 -2,-2zM21,16L3,16L3,8h2v4h2L7,8h2v4h2L11,8h2v4h2L15,8h2v4h2L19,8h2v8z",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Ruler!!
    }

@Suppress("ObjectPropertyName")
private var _Ruler: ImageVector? = null
