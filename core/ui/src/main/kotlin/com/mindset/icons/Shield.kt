package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

val MindSetIcons.Shield: ImageVector
    get() {
        if (_Shield != null) return _Shield!!
        _Shield =
            ImageVector
                .Builder(
                    name = "Shield",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).addPath(
                    pathData =
                    PathParser()
                        .parsePathString(
                            "M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12L21,5l-9,-4z",
                        ).toNodes(),
                    fill = SolidColor(Color.White),
                ).build()
        return _Shield!!
    }

@Suppress("ObjectPropertyName")
private var _Shield: ImageVector? = null
