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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import dev.cadence.icons.Add
import dev.cadence.icons.ArrowBack
import dev.cadence.icons.ChevronRight
import dev.cadence.icons.Dumbbell
import dev.cadence.icons.Search
import dev.cadence.domain.ExerciseFilters
import dev.cadence.model.Exercise
import dev.cadence.presentation.ExerciseLibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

/*
 * Exercise picker — searchable, filterable list reached as a push from Log Workout / the Template
 * Builder. Restyled to the Kinetic Precision system: a pinned translucent glass header (a frosted
 * search field + muscle / equipment selector chips, matching the Hyrox sim) floats over a scrolling
 * list of bordered exercise cards. The list re-queries as the query or a filter changes.
 */

private val Gutter = 20.dp

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
        val colors = MaterialTheme.colorScheme
        Scaffold(containerColor = colors.background) { inner ->
            Box(Modifier.fillMaxSize()) {
                // The list scrolls under the glass header; contentPadding clears the measured header.
                var headerHeight by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = headerHeight,
                        bottom = inner.calculateBottomPadding() + 16.dp,
                        start = Gutter,
                        end = Gutter,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(exercises.itemCount) { index ->
                        val exercise = exercises[index] ?: return@items
                        ExerciseCard(exercise, onClick = { onOpenDetail(exercise.id) })
                    }
                }

                // Pinned translucent glass header (back + title, frosted search, filter chips). The
                // 0.9-alpha background lets list rows show faintly through as they scroll under it —
                // the frosted top-bar treatment from the Hyrox sim.
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .onSizeChanged { headerHeight = with(density) { it.height.toDp() } }
                        .background(colors.background.copy(alpha = 0.9f)),
                ) {
                    Spacer(Modifier.height(inner.calculateTopPadding()))
                    TopBar(onBack = onBack)
                    SearchField(
                        query = query,
                        onQueryChange = libraryViewModel::onQueryChange,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    ChipRow(
                        options = ExerciseFilters.muscles,
                        selected = selectedMuscle,
                        onToggle = libraryViewModel::onMuscleToggle,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    ChipRow(
                        options = ExerciseFilters.equipment,
                        selected = selectedEquipment,
                        onToggle = libraryViewModel::onEquipmentToggle,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

// ── Header: top bar / search / chips ────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(CadenceIcons.ArrowBack, contentDescription = "Back", tint = colors.onSurface, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.size(4.dp))
        Text("Add Exercise", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter)
            .height(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerHigh.copy(alpha = 0.7f))
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(CadenceIcons.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    "Search name, muscle, equipment",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            // A plus rotated 45° reads as a clear (×) glyph (no dedicated close icon in the set).
            Icon(
                CadenceIcons.Add,
                contentDescription = "Clear search",
                tint = colors.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(45f)
                    .clip(CircleShape)
                    .clickable { onQueryChange("") },
            )
        }
    }
}

@Composable
private fun ChipRow(
    options: List<String>,
    selected: String?,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Gutter),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            SelectorChip(
                label = option.replaceFirstChar { it.uppercase() },
                selected = option == selected,
                onClick = { onToggle(option) },
            )
        }
    }
}

/** Pill selector chip, matching the Hyrox sim: primaryContainer when active, otherwise an outlined
 *  surface. */
@Composable
private fun SelectorChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val base = Modifier.clip(CircleShape)
    val styled = if (selected) {
        base.background(colors.primaryContainer)
    } else {
        base.background(colors.surfaceContainer).border(1.dp, colors.outlineVariant, CircleShape)
    }
    Box(
        modifier = styled.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
        )
    }
}

// ── Exercise card ───────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumbnail(exercise.imageUrls.firstOrNull())
        Column(Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(
                subtitle(exercise.primaryMuscles.firstOrNull(), exercise.equipment),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Icon(
            CadenceIcons.ChevronRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(7.4.dp, 12.dp),
        )
    }
}

/** Rounded thumbnail tile; falls back to a muted dumbbell glyph when the exercise has no image. */
@Composable
private fun Thumbnail(imageUrl: String?) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(CadenceIcons.Dumbbell, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun subtitle(muscle: String?, equipment: String?): String =
    listOfNotNull(muscle, equipment)
        .map { it.replaceFirstChar { c -> c.uppercase() } }
        .joinToString(" · ")
