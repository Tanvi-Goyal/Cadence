package com.mindset.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons
import com.mindset.icons.Add

val LocalQuickStart = staticCompositionLocalOf { {} }

@Composable
fun QuickStartFab(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    FloatingActionButton(
        onClick = onClick,
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
        modifier = Modifier
    ) {
        Icon(
            imageVector = MindSetIcons.Add,
            contentDescription = "Quick start",
            tint = colors.onPrimary,
            modifier = Modifier.size(24.dp),
        )
    }
}
