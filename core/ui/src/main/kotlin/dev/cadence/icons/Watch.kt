package dev.cadence.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import dev.cadence.CadenceIcons

/** Watch glyph for the "Wearable Devices" integration. */
val CadenceIcons.Watch: ImageVector
    get() {
        if (_Watch != null) return _Watch!!
        _Watch = ImageVector.Builder(
            name = "Watch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M20,12c0,-2.54 -1.19,-4.81 -3.04,-6.27L16,0L8,0l-0.95,5.73C5.19,7.19 4,9.45 4,12s1.19,4.81 " +
                    "3.05,6.27L8,24h8l0.96,-5.73C18.81,16.81 20,14.54 20,12zM6,12c0,-3.31 2.69,-6 6,-6s6,2.69 " +
                    "6,6 -2.69,6 -6,6 -6,-2.69 -6,-6z",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Watch!!
    }

@Suppress("ObjectPropertyName")
private var _Watch: ImageVector? = null
