package dev.cadence.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import dev.cadence.CadenceIcons

val CadenceIcons.Menu: ImageVector
    get() {
        if (_Menu != null) return _Menu!!
        _Menu = ImageVector.Builder(
            name = "Menu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M3,6h18v2H3z M3,11h18v2H3z M3,16h18v2H3z",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Menu!!
    }

@Suppress("ObjectPropertyName")
private var _Menu: ImageVector? = null
