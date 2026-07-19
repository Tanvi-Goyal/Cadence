package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.presentation.TemplateUi
import dev.cadence.presentation.TemplatesViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Templates surface: a list of the user's templates. Each can be started (deep-copied into a
 * fresh session opened in Log Workout) or tapped to edit in the builder.
 */
@Composable
fun TemplatesScreen(
    onBack: () -> Unit,
    onNewTemplate: () -> Unit,
    onEditTemplate: (String) -> Unit,
    onStarted: (String) -> Unit,
    viewModel: TemplatesViewModel = koinViewModel(),
) {
    val templates by viewModel.uiState.collectAsStateWithLifecycle()
    CadenceTheme {
        Scaffold(containerColor = Background) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "‹",
                            color = TextPrimary,
                            fontSize = 28.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                        )
                        Spacer(Modifier.size(12.dp))
                        Text("Templates", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, TextSecondary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onNewTemplate)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+ New template", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
                if (templates.isEmpty()) {
                    item {
                        Text(
                            "No templates yet. Build one to start it any day.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                } else {
                    items(templates, key = { it.id }) { template ->
                        TemplateRow(
                            template = template,
                            onEdit = { onEditTemplate(template.id) },
                            onStart = { viewModel.startTemplate(template.id, onStarted) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateRow(template: TemplateUi, onEdit: () -> Unit, onStart: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable(onClick = onEdit)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceHi),
            contentAlignment = Alignment.Center,
        ) {
            Text(typeBadge(template.type), color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(template.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Tap to edit", color = TextSecondary, fontSize = 13.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Accent)
                .clickable(onClick = onStart)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("Start", color = OnAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
