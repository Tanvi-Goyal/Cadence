package com.mindset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mindset.GlassBorder
import com.mindset.GlassFill
import com.mindset.spacing

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    var base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(GlassFill)
        .border(1.dp, GlassBorder, shape)
    if (onClick != null) base = base.clickable(onClick = onClick)
    Box(
        base.padding(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.smd,
        )
    ) { content() }

}

