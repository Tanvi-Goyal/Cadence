package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.domain.LoggedItemUi
import com.mindset.domain.Units
import com.mindset.domain.detailSummary
import com.mindset.icons.ArrowBack
import com.mindset.presentation.SessionDetailUiState
import com.mindset.presentation.SessionDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionDetailScreen(sessionId: String, onBack: () -> Unit, viewModel: SessionDetailViewModel = koinViewModel { parametersOf(sessionId) }) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        Scaffold(containerColor = colors.background) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.md,
                    end = MaterialTheme.spacing.md,
                    top = padding.calculateTopPadding() + MaterialTheme.spacing.smd,
                    bottom = padding.calculateBottomPadding() + MaterialTheme.spacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
            ) {
                item(key = "header") { DetailHeader(state, onBack) }

                if (state.items.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "No exercises logged.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.items, key = { it.loggedItemId }) { item -> DetailCard(item) }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(state: SessionDetailUiState, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(colors.surfaceContainerHigh)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                MindSetIcons.ArrowBack,
                contentDescription = "Back",
                tint = colors.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
        Column {
            Text(
                state.name.ifEmpty { "Session" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Text(
                "${relativeDate(state.startedAt)} · ${formatVolume(state.totalVolumeKg, unit)} ${Units.label(unit)}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailCard(item: LoggedItemUi) {
    val colors = MaterialTheme.colorScheme
    val unit = LocalWeightUnit.current
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).background(GlassFill)
            .border(1.dp, GlassBorder, shape).padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Text(
            item.exerciseName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${item.sets.size} sets",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        item.sets.forEach { set ->
            Text(
                set.detailSummary(item.captureFields, unit),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
        }
    }
}
