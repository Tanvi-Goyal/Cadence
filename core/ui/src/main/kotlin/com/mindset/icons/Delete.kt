package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Trash glyph for the destructive "Delete Account" row. */
val MindSetIcons.Delete: ImageVector
    get() {
        if (_Delete != null) return _Delete!!
        _Delete =
            ImageVector
                .Builder(
                    name = "Delete",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).addPath(
                    pathData =
                    PathParser()
                        .parsePathString(
                            "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2L18,7L6,7v12zM19,4h-3.5l-1,-1h-5l-1,1L5,4v2h14L19,4z",
                        ).toNodes(),
                    fill = SolidColor(Color.White),
                ).build()
        return _Delete!!
    }

@Suppress("ObjectPropertyName")
private var _Delete: ImageVector? = null
