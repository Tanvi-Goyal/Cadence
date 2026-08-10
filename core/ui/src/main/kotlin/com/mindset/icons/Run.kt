package com.mindset.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

/** Running figure — the run / cardio glyph. Tinted at the call site. */
val MindSetIcons.Run: ImageVector
    get() {
        if (_Run != null) return _Run!!
        _Run = ImageVector.Builder(
            name = "Run",
            defaultWidth = 22.dp,
            defaultHeight = 16.dp,
            viewportWidth = 22f,
            viewportHeight = 16f,
        ).addPath(
            pathData = PathParser().parsePathString(
                "M4.4 16L3 14.6L12.6 5H10V7H8V3H13.825C14.0917 3 14.35 3.05 14.6 3.15C14.85 3.25 " +
                    "15.0667 3.39167 15.25 3.575L18.25 6.55C18.7 7 19.25 7.35 19.9 7.6C20.55 7.85 " +
                    "21.25 7.98333 22 8V10C20.9667 10 20.0292 9.84167 19.1875 9.525C18.3458 9.20833 " +
                    "17.6 8.73333 16.95 8.1L15.95 7.05L13.75 9.25L16 11.5L9.45 15.275L8.45 " +
                    "13.55L12.75 11.075L11.05 9.375L4.4 16V16M2 9V7H7V9H2V9M0 6V4H5V6H0V6M18.475 " +
                    "4C17.925 4 17.45 3.80417 17.05 3.4125C16.65 3.02083 16.45 2.55 16.45 2C16.45 " +
                    "1.45 16.65 0.979167 17.05 0.5875C17.45 0.195833 17.925 0 18.475 0C19.025 0 19.5 " +
                    "0.195833 19.9 0.5875C20.3 0.979167 20.5 1.45 20.5 2C20.5 2.55 20.3 3.02083 19.9 " +
                    "3.4125C19.5 3.80417 19.025 4 18.475 4V4M2 3V1H7V3H2V3",
            ).toNodes(),
            fill = SolidColor(Color.White),
        ).build()
        return _Run!!
    }

@Suppress("ObjectPropertyName")
private var _Run: ImageVector? = null
