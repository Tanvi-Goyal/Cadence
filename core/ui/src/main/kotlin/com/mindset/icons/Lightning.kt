package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Lightning bolt — the high-intensity / conditioning glyph. Tinted at the call site. */
val MindSetIcons.Lightning: ImageVector
    get() {
        if (_Lightning != null) return _Lightning!!
        _Lightning = ImageVector.Builder(
            name = "Lightning",
            defaultWidth = 16.dp,
            defaultHeight = 20.dp,
            viewportWidth = 16f,
            viewportHeight = 20f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M6.55 16.2L11.725 10H7.725L8.45 4.325L3.825 11H7.3L6.55 16.2V16.2M4 20L5 13H0L9 " +
                    "0H11L10 8H16L6 20H4V20M7.775 10.25V10.25V10.25V10.25V10.25V10.25V10.25V10.25",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Lightning!!
    }

@Suppress("ObjectPropertyName")
private var _Lightning: ImageVector? = null
