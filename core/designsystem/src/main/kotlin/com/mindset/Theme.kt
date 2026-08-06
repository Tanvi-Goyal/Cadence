package com.mindset

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.mindset.domain.ThemeMode
import com.mindset.domain.WeightUnit

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
val LocalWeightUnit = staticCompositionLocalOf { WeightUnit.KG }

@Composable
fun MindSetTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = MindSetColorScheme,
            typography = ObsidianTypography,
            shapes = MindSetShapes,
            content = content,
        )
    }
}
