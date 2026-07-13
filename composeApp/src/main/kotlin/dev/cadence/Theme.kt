package dev.cadence

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cadence dark palette (from the Figma design), shared across all screens.
internal val Background = Color(0xFF0E0E0F)
internal val Surface = Color(0xFF1A1A1C)
internal val SurfaceHi = Color(0xFF232326)
internal val Accent = Color(0xFFC7F04D)
internal val OnAccent = Color(0xFF14210A)
internal val TextPrimary = Color(0xFFF5F5F5)
internal val TextSecondary = Color(0xFF8E8E93)
internal val Danger = Color(0xFFE24B4A)

private val CadenceColors = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
internal fun CadenceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CadenceColors, content = content)
}
