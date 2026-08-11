package com.mindset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mindset.MindSetWordmark
import com.mindset.icons.NavAccount

@Composable
fun MindSetTopBar(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface),
        title = {
            MindSetWordmark()
        },
        navigationIcon = {},
        actions = {
            IconButton(
                onClick = onProfileClick,
            ) {
                Icon(
                    imageVector = NavAccount,
                    contentDescription = "Profile",
                    tint = colors.onSurface,
                    modifier = Modifier,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            scrolledContainerColor = colors.surface,
        ),
    )
}
