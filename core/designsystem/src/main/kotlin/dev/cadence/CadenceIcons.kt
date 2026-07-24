package dev.cadence

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/*
 * In-code icon set — the code-based alternative to the vector drawables in res/drawable/ic_*.xml.
 * Both are built from the SAME path geometry exported from the Figma design, so they render
 * identically; this set exists so we can compare an in-code ImageVector approach against
 * painterResource(R.drawable.ic_*). Icons are authored in white and tinted at the call site via
 * Icon(tint = ...). Built lazily so unused icons cost nothing.
 */
object CadenceIcons {

    private fun icon(name: String, w: Float, h: Float, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = w.dp,
            defaultHeight = h.dp,
            viewportWidth = w,
            viewportHeight = h,
        ).addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            fill = SolidColor(Color.White),
        ).build()

    val Sync: ImageVector by lazy {
        icon(
            "Sync", 16f, 16f,
            "M0 16V14H2.75L2.35 13.65C1.48333 12.8833 0.875 12.0083 0.525 11.025C0.175 10.0417 0 9.05 0 8.05C0 6.2 0.554167 4.55417 1.6625 3.1125C2.77083 1.67083 4.21667 0.716667 6 0.25V2.35C4.8 2.78333 3.83333 3.52083 3.1 4.5625C2.36667 5.60417 2 6.76667 2 8.05C2 8.8 2.14167 9.52917 2.425 10.2375C2.70833 10.9458 3.15 11.6 3.75 12.2L4 12.45V10H6V16H0V16M10 15.75V13.65C11.2 13.2167 12.1667 12.4792 12.9 11.4375C13.6333 10.3958 14 9.23333 14 7.95C14 7.2 13.8583 6.47083 13.575 5.7625C13.2917 5.05417 12.85 4.4 12.25 3.8L12 3.55V6H10V0H16V2H13.25L13.65 2.35C14.4667 3.16667 15.0625 4.05417 15.4375 5.0125C15.8125 5.97083 16 6.95 16 7.95C16 9.8 15.4458 11.4458 14.3375 12.8875C13.2292 14.3292 11.7833 15.2833 10 15.75V15.75",
        )
    }

    val Lightning: ImageVector by lazy {
        icon(
            "Lightning", 16f, 20f,
            "M6.55 16.2L11.725 10H7.725L8.45 4.325L3.825 11H7.3L6.55 16.2V16.2M4 20L5 13H0L9 0H11L10 8H16L6 20H4V20M7.775 10.25V10.25V10.25V10.25V10.25V10.25V10.25V10.25",
        )
    }

    val Run: ImageVector by lazy {
        icon(
            "Run", 22f, 16f,
            "M4.4 16L3 14.6L12.6 5H10V7H8V3H13.825C14.0917 3 14.35 3.05 14.6 3.15C14.85 3.25 15.0667 3.39167 15.25 3.575L18.25 6.55C18.7 7 19.25 7.35 19.9 7.6C20.55 7.85 21.25 7.98333 22 8V10C20.9667 10 20.0292 9.84167 19.1875 9.525C18.3458 9.20833 17.6 8.73333 16.95 8.1L15.95 7.05L13.75 9.25L16 11.5L9.45 15.275L8.45 13.55L12.75 11.075L11.05 9.375L4.4 16V16M2 9V7H7V9H2V9M0 6V4H5V6H0V6M18.475 4C17.925 4 17.45 3.80417 17.05 3.4125C16.65 3.02083 16.45 2.55 16.45 2C16.45 1.45 16.65 0.979167 17.05 0.5875C17.45 0.195833 17.925 0 18.475 0C19.025 0 19.5 0.195833 19.9 0.5875C20.3 0.979167 20.5 1.45 20.5 2C20.5 2.55 20.3 3.02083 19.9 3.4125C19.5 3.80417 19.025 4 18.475 4V4M2 3V1H7V3H2V3",
        )
    }
}
