package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Database cylinder for the "Data Export" action. */
val MindSetIcons.Database: ImageVector
    get() {
        if (_Database != null) return _Database!!
        _Database = ImageVector.Builder(
            name = "Database",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M12,3C7.58,3 4,4.34 4,6v12c0,1.66 3.58,3 8,3s8,-1.34 8,-3L20,6c0,-1.66 -3.58,-3 " +
                    "-8,-3zM18,18c-0.02,0.22 -1.91,1 -6,1 -4.13,0 -6,-0.79 -6,-1v-2.23c1.61,0.78 " +
                    "4.09,1.23 6,1.23s4.39,-0.45 6,-1.23L18,18zM18,12.5c-0.02,0.22 -1.91,1 -6,1 " +
                    "-4.13,0 -6,-0.79 -6,-1v-2.5C7.61,10.78 10.09,11.5 12,11.5s4.39,-0.72 6,-1.5v2.5zM12,9c-4.13,0 " +
                    "-6,-0.79 -6,-1s1.87,-1 6,-1 6,0.79 6,1 -1.87,1 -6,1z",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Database!!
    }

@Suppress("ObjectPropertyName")
private var _Database: ImageVector? = null
