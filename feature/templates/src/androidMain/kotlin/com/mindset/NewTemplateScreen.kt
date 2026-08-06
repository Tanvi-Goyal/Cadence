package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindset.model.SessionType
import com.mindset.presentation.TemplatesViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Create a new template: pick a focus (reusing the New Session type grid) and give it a name, then
 * hand off to the builder to add exercises + target sets.
 */
@Composable
fun NewTemplateScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: TemplatesViewModel = koinViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(SessionType.STRENGTH.name) }
    MindSetTheme {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "‹",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("New template", color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("A plan you can start any day", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("NAME", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Upper A", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Spacer(Modifier.height(24.dp))
                Text("SESSION TYPE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                typeOptions.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { option ->
                            TypeCard(
                                option = option,
                                selected = option.type == selected,
                                onClick = { selected = option.type },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.weight(1f))
                val trimmed = name.trim()
                Button(
                    onClick = {
                        val finalName = trimmed.ifEmpty { defaultTemplateName(selected) }
                        viewModel.createTemplate(finalName, selected, onCreated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Create template", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun defaultTemplateName(type: String): String = when (type) {
    SessionType.CONDITIONING.name -> "Conditioning template"
    SessionType.HYROX.name -> "Hyrox template"
    SessionType.MIXED.name -> "Mixed template"
    else -> "Strength template"
}
