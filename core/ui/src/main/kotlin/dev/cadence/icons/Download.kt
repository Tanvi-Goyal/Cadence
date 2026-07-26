package dev.cadence.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import dev.cadence.CadenceIcons

val CadenceIcons.Download: ImageVector
    get() {
        if (_Download != null) return _Download!!
        _Download = ImageVector.Builder(
            name = "Download",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M19,9h-4L15,3L9,3v6L5,9l7,7 7,-7zM5,18v2h14v-2L5,18z",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Download!!
    }

@Suppress("ObjectPropertyName")
private var _Download: ImageVector? = null
