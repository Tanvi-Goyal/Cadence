package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.cadence.presentation.ExerciseLibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExercisePickerScreen(
    onPick: (String) -> Unit,
    onBack: () -> Unit,
    libraryViewModel: ExerciseLibraryViewModel = koinViewModel(),
) {
    val query by libraryViewModel.queryText.collectAsStateWithLifecycle()
    val exercises = libraryViewModel.exercises.collectAsLazyPagingItems()

    CadenceTheme {
        Scaffold(containerColor = Background) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "‹",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                    )
                    Spacer(Modifier.size(12.dp))
                    Text("Add exercise", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = libraryViewModel::onQueryChange,
                    placeholder = { Text("Search exercises", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
                Spacer(Modifier.size(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exercises.itemCount) { index ->
                        val exercise = exercises[index] ?: return@items
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Surface)
                                .clickable { onPick(exercise.id) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(exercise.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(exercise.category, color = TextSecondary, fontSize = 12.sp)
                            }
                            Text("+", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
