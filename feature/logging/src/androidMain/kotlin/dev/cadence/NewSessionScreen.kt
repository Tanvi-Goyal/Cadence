package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.cadence.model.SessionType
import dev.cadence.presentation.NewSessionViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun NewSessionScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: NewSessionViewModel = koinViewModel(),
) {
    var selected by remember { mutableStateOf(SessionType.STRENGTH.name) }
    CadenceTheme {
        Scaffold(containerColor = Background) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "‹",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("New session", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Pick a focus to start", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("SESSION TYPE", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                // 2×2 grid of type cards.
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
                Button(
                    onClick = { viewModel.create(selected, onCreated) },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

