package dev.cadence

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Compact numeric text field shared by the logging + template-building screens. The [placeholder]
 * doubles as the field's hint — for a ghost target it's the prescribed value (e.g. "5"), rendered
 * in the muted [TextSecondary] so an unfilled actual reads as a suggestion, not data.
 */
@Composable
internal fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceHi,
            unfocusedContainerColor = SurfaceHi,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}
