package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import dev.cadence.domain.ExerciseFilters
import dev.cadence.presentation.ExerciseLibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExercisePickerScreen(
    onOpenDetail: (String) -> Unit,
    onBack: () -> Unit,
    libraryViewModel: ExerciseLibraryViewModel = koinViewModel(),
) {
    val query by libraryViewModel.queryText.collectAsStateWithLifecycle()
    val selectedMuscle by libraryViewModel.selectedMuscle.collectAsStateWithLifecycle()
    val selectedEquipment by libraryViewModel.selectedEquipment.collectAsStateWithLifecycle()
    val exercises = libraryViewModel.exercises.collectAsLazyPagingItems()

    CadenceTheme {
        Scaffold(containerColor = Background) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().background(Background).padding(padding).padding(horizontal = 20.dp),
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
                    placeholder = { Text("Search name, muscle, equipment", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
                Spacer(Modifier.size(10.dp))
                ChipRow(ExerciseFilters.muscles, selectedMuscle, libraryViewModel::onMuscleToggle)
                Spacer(Modifier.size(6.dp))
                ChipRow(ExerciseFilters.equipment, selectedEquipment, libraryViewModel::onEquipmentToggle)
                Spacer(Modifier.size(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exercises.itemCount) { index ->
                        val exercise = exercises[index] ?: return@items
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Surface)
                                .clickable { onOpenDetail(exercise.id) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = exercise.imageUrlsList.firstOrNull(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceHi),
                            )
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exercise.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    subtitle(exercise.primaryMusclesList.firstOrNull(), exercise.equipment),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                )
                            }
                            Text("›", color = TextSecondary, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun subtitle(muscle: String?, equipment: String?): String =
    listOfNotNull(muscle?.replaceFirstChar { it.uppercase() }, equipment).joinToString(" · ")

@Composable
private fun ChipRow(options: List<String>, selected: String?, onToggle: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            val isOn = option == selected
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isOn) Accent else SurfaceHi)
                    .clickable { onToggle(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    option.replaceFirstChar { it.uppercase() },
                    color = if (isOn) OnAccent else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isOn) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
