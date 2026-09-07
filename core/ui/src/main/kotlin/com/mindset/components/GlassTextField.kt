package com.mindset.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun GlassTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, style: TextStyle = MaterialTheme.typography.bodyLarge) {
    val colors = MaterialTheme.colorScheme
    GlassCard {
        Box {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = style,
                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = style.copy(color = colors.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
