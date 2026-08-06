package com.mindset

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.icons.ArrowBack
import com.mindset.icons.ChevronRight
import com.mindset.icons.CoolDown
import com.mindset.icons.MoreVert
import com.mindset.icons.Play
import com.mindset.icons.Share
import com.mindset.icons.WarmUp
import com.mindset.presentation.StrengthExerciseUi
import com.mindset.presentation.StrengthSectionUi
import com.mindset.presentation.TemplateStrengthDetailUiState
import com.mindset.presentation.TemplateStrengthDetailViewModel
import com.mindset.ui.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/*
 * Template Detail — Strength (DB-backed). Renders a seeded program day observed from the DB onto the
 * Obsidian design: hero + focus-area chips + session-goal card + warm-up card + the exercise sections
 * (main / accessory / conditioning) + an expandable cool-down. START WORKOUT deep-copies the template
 * into a live session (the VM) and opens Log Workout.
 */

private val Gutter = 20.dp

@Composable
fun TemplateStrengthDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onStarted: (String) -> Unit,
    onEdit: () -> Unit,
    viewModel: TemplateStrengthDetailViewModel = koinViewModel { parametersOf(templateId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        TemplateStrengthDetailContent(
            state = state,
            onBack = onBack,
            onStart = { viewModel.start(onStarted) },
            onEdit = onEdit,
            onToggleCoolDown = viewModel::onToggleCoolDown,
        )
    }
}

@Composable
private fun TemplateStrengthDetailContent(
    state: TemplateStrengthDetailUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onToggleCoolDown: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(containerColor = colors.background) { inner ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = inner.calculateBottomPadding() + 120.dp),
            ) {
                item(key = "hero") { HeroHeader(state) }
                item(key = "body") {
                    Column(
                        modifier = Modifier.padding(top = 24.dp, start = Gutter, end = Gutter),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        if (state.focusAreas.isNotEmpty()) FocusAreas(state.focusAreas)
                        state.sessionGoal?.let { SessionGoalCard(it) }
                        state.warmUp?.let { SectionBlock(it, MindSetIcons.WarmUp) }
                        state.sections.forEach { SectionBlock(it) }
                        state.coolDown?.let { CoolDownCard(it, state.coolDownExpanded, onToggleCoolDown) }
                    }
                }
            }

            TopBar(onBack, onEdit, Modifier.align(Alignment.TopStart).padding(top = inner.calculateTopPadding()))
            if (state.loaded) {
                StartCta(onStart, Modifier.align(Alignment.BottomStart).padding(bottom = inner.calculateBottomPadding()))
            }
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(state: TemplateStrengthDetailUiState) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(300.dp)) {
        Image(
            painter = painterResource(R.drawable.template_interval_run), // TODO(images): per-day photos
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 0.5f to colors.background.copy(alpha = 0.3f), 1f to colors.background)))
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(Gutter),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroTag("Strength")
                if (state.focus.isNotBlank()) HeroTag(state.focus)
            }
            Text(state.title.ifBlank { "Loading…" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            if (state.loaded) {
                Text("${state.exerciseCount} exercises", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HeroTag(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.clip(CircleShape).background(colors.primary.copy(alpha = 0.1f)).border(1.dp, colors.primary.copy(alpha = 0.2f), CircleShape).padding(horizontal = 13.dp, vertical = 5.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, color = colors.primary)
    }
}

// ── Focus areas ────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusAreas(areas: List<String>) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("Focus Areas")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            areas.forEach { area ->
                Box(
                    Modifier.clip(MaterialTheme.shapes.small).background(colors.surfaceContainer).border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small).padding(horizontal = 13.dp, vertical = 8.dp),
                ) {
                    Text(area, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                }
            }
        }
    }
}

// ── Session goal ───────────────────────────────────────────────────────────────────────────────

@Composable
private fun SessionGoalCard(goal: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.primary.copy(alpha = 0.05f)).border(1.dp, colors.primary.copy(alpha = 0.2f), MaterialTheme.shapes.medium).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Session Goal".uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp, color = colors.primary)
        Text(goal, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = colors.onSurfaceVariant)
    }
}

// ── Sections ──────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionBlock(section: StrengthSectionUi, leadingIcon: ImageVector? = null) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            Text(section.label.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = colors.onSurfaceVariant)
            if (section.meta != null) MetaPill(section.meta)
        }
        ExerciseCard(section.exercises)
    }
}

@Composable
private fun MetaPill(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.clip(CircleShape).background(colors.primary.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = colors.primary)
    }
}

@Composable
private fun ExerciseCard(exercises: List<StrengthExerciseUi>) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.surfaceContainer).border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
    ) {
        exercises.forEachIndexed { i, ex ->
            if (i > 0) HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
            ExerciseRow(ex)
        }
    }
}

@Composable
private fun ExerciseRow(ex: StrengthExerciseUi) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(ex.name, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                if (ex.eachSide) {
                    Box(Modifier.clip(CircleShape).background(colors.surfaceContainerHighest).padding(horizontal = 6.dp, vertical = 1.dp)) {
                        Text("ES", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            if (!ex.note.isNullOrBlank()) Text(ex.note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        if (ex.scheme.isNotBlank()) {
            Text(ex.scheme, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.primary)
        }
    }
}

// ── Cool-down (expandable) ────────────────────────────────────────────────────────────────────

@Composable
private fun CoolDownCard(section: StrengthSectionUi, expanded: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, label = "coolChevron")
    Column(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.surfaceContainer).border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium).animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.small).background(colors.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                Icon(MindSetIcons.CoolDown, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(section.label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                Text("${section.exercises.size} exercises", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Icon(MindSetIcons.ChevronRight, contentDescription = if (expanded) "Collapse" else "Expand", tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp).rotate(rotation))
        }
        if (expanded) {
            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
            Column(Modifier.padding(vertical = 4.dp)) {
                section.exercises.forEachIndexed { i, ex ->
                    if (i > 0) HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.15f))
                    ExerciseRow(ex)
                }
            }
        }
    }
}

// ── Top bar / CTA ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onBack: () -> Unit, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassButton(MindSetIcons.ArrowBack, 16.dp, onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassButton(MindSetIcons.Share, 18.dp, onClick = {})
            GlassButton(MindSetIcons.MoreVert, 16.dp, onClick = onEdit)
        }
    }
}

@Composable
private fun GlassButton(icon: ImageVector, size: Dp, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.surfaceContainer.copy(alpha = 0.5f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(size))
    }
}

@Composable
private fun StartCta(onStart: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxWidth().background(Brush.verticalGradient(0f to Color.Transparent, 0.5f to colors.background, 1f to colors.background)).padding(top = 40.dp, bottom = 20.dp, start = Gutter, end = Gutter),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(16.dp, CircleShape).clip(CircleShape).background(colors.primary).clickable(onClick = onStart),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(MindSetIcons.Play, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(11.dp, 14.dp))
            Spacer(Modifier.width(12.dp))
            Text("Start Workout".uppercase(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = colors.onPrimary)
        }
    }
}
