package dev.cadence

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.icons.ArrowBack
import dev.cadence.icons.Barbell
import dev.cadence.icons.ChevronRight
import dev.cadence.icons.CoolDown
import dev.cadence.icons.Dumbbell
import dev.cadence.icons.Edit
import dev.cadence.icons.Flame
import dev.cadence.icons.LowerBody
import dev.cadence.icons.MoreVert
import dev.cadence.icons.Play
import dev.cadence.icons.Share
import dev.cadence.icons.Stretch
import dev.cadence.icons.Timer
import dev.cadence.icons.WarmUp
import dev.cadence.presentation.BlockGlyph
import dev.cadence.presentation.DetailTag
import dev.cadence.presentation.ExerciseGroup
import dev.cadence.presentation.ProtocolItem
import dev.cadence.presentation.StrengthExercise
import dev.cadence.presentation.StrengthGlyph
import dev.cadence.presentation.StrengthSession
import dev.cadence.presentation.StrengthStat
import dev.cadence.presentation.TemplateStrengthDetailUiState
import dev.cadence.presentation.TemplateStrengthDetailViewModel
import dev.cadence.ui.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/*
 * Template Detail — Strength (Push / Pull / Lower Body). The strength counterpart to the interval and
 * HYROX detail screens: a full-bleed photo hero, a focus-area chip row, a session-goal card, a warm-up
 * row, the exercise list (supersets share a lime accent spine; each exercise carries a TARGET / PREV /
 * RPE / TEMPO stat table), an expandable cool-down card, and the pinned START WORKOUT CTA.
 *
 * Content is derived by [TemplateStrengthDetailViewModel] from the template id; layout is stateless
 * ([TemplateStrengthDetailContent]) so it previews and tests without Koin.
 */

// Spacing — a single source of truth so blocks are evenly spaced (the mock has ad-hoc gaps). Gutter
// matches the sibling detail screens; the rest sit on the 8dp grid.
private val Gutter = 20.dp
private val SectionGap = 24.dp
private val CardPad = 16.dp
private val RowGap = 8.dp
private val Medallion = 48.dp

@Composable
fun TemplateStrengthDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    viewModel: TemplateStrengthDetailViewModel = koinViewModel { parametersOf(templateId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CadenceTheme {
        TemplateStrengthDetailContent(
            state = state,
            onBack = onBack,
            onStart = onStart,
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
                contentPadding = PaddingValues(bottom = inner.calculateBottomPadding() + 132.dp),
            ) {
                item(key = "hero", contentType = "hero") { HeroHeader(state) }
                item(key = "body", contentType = "body") {
                    Column(
                        modifier = Modifier.padding(top = SectionGap, start = Gutter, end = Gutter),
                        verticalArrangement = Arrangement.spacedBy(SectionGap),
                    ) {
                        FocusAreas(state.focusAreas)
                        SessionGoalCard(state.sessionGoal)
                        ProtocolRow(state.warmUp)
                        ExercisesSection(state.groups, onEdit)
                        CoolDownCard(state, onToggleCoolDown)
                    }
                }
            }

            TopBar(
                onBack = onBack,
                onEdit = onEdit,
                modifier = Modifier.align(Alignment.TopStart).padding(top = inner.calculateTopPadding()),
            )

            StartCta(
                onStart = onStart,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = inner.calculateBottomPadding()),
            )
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(state: TemplateStrengthDetailUiState) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(340.dp)) {
        Image(
            // TODO(images): per-session photos land here — user is providing template_strength_{push,pull,lower}.
            painter = painterResource(heroImageFor(state.session)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.5f to colors.background.copy(alpha = 0.2f),
                    1.0f to colors.background,
                ),
            ),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(Gutter),
            verticalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
                state.tags.forEach { HeroTag(it) }
            }
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.4).sp,
                color = colors.onSurface,
            )
            Text(state.subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(CardPad), verticalAlignment = Alignment.CenterVertically) {
                MetaStat(CadenceIcons.Timer, state.duration, 15.dp)
                MetaStat(CadenceIcons.Flame, state.calories, 14.dp)
            }
        }
    }
}

@Composable
private fun HeroTag(tag: DetailTag) {
    val colors = MaterialTheme.colorScheme
    val fill = if (tag.danger) colors.errorContainer else colors.primary
    val stroke = if (tag.danger) colors.error else colors.primary
    val text = if (tag.danger) colors.error else colors.primary
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(fill.copy(alpha = 0.1f))
            .border(1.dp, stroke.copy(alpha = 0.2f), CircleShape)
            .padding(horizontal = 13.dp, vertical = 5.dp),
    ) {
        Text(tag.text.uppercase(), style = MaterialTheme.typography.labelLarge, color = text)
    }
}

@Composable
private fun MetaStat(icon: ImageVector, label: String, iconSize: Dp) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(RowGap / 2), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(iconSize))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

// ── Focus areas ────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusAreas(areas: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(RowGap + 4.dp)) {
        SectionLabel("Focus Areas")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(RowGap),
            verticalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            areas.forEach { FocusChip(it) }
        }
    }
}

@Composable
private fun FocusChip(label: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
            .padding(horizontal = 13.dp, vertical = RowGap),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
    }
}

// ── Session goal ───────────────────────────────────────────────────────────────────────────────

@Composable
private fun SessionGoalCard(goal: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.primary.copy(alpha = 0.05f))
            .border(1.dp, colors.primary.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
            .padding(CardPad),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        Text(
            "Session Goal".uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp,
            color = colors.primary,
        )
        Text(goal, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = colors.onSurfaceVariant)
    }
}

// ── Warm-up / cool-down rows ─────────────────────────────────────────────────────────────────

/** A muted protocol row: a squared medallion, title + detail, and a right-aligned duration. Used for
 *  the warm-up and each cool-down stretch. */
@Composable
private fun ProtocolRow(item: ProtocolItem, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(CardPad),
        horizontalArrangement = Arrangement.spacedBy(CardPad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphMedallion(protocolIcon(item.glyph), tint = colors.onSurfaceVariant)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                Text(item.duration, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

// ── Exercises ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExercisesSection(groups: List<ExerciseGroup>, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(CardPad)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("Exercises")
            Row(
                modifier = Modifier.clip(CircleShape).clickable(onClick = onEdit).padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(RowGap / 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(CadenceIcons.Edit, contentDescription = null, tint = colors.primary, modifier = Modifier.size(13.dp))
                Text("Edit Order", style = MaterialTheme.typography.bodyMedium, color = colors.primary)
            }
        }
        groups.forEach { ExerciseGroupCard(it) }
    }
}

@Composable
private fun ExerciseGroupCard(group: ExerciseGroup) {
    val colors = MaterialTheme.colorScheme
    val superset = group.exercises.size > 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
    ) {
        // Supersets share a single lime accent spine down the left; standalone lifts have none.
        if (superset) Box(Modifier.width(4.dp).fillMaxHeight().background(colors.primary))
        Column(Modifier.weight(1f)) {
            group.exercises.forEachIndexed { index, exercise ->
                if (index > 0) {
                    HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
                }
                ExerciseRow(exercise)
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: StrengthExercise) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.padding(CardPad), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(CardPad), verticalAlignment = Alignment.CenterVertically) {
                GlyphMedallion(exerciseIcon(exercise.glyph), tint = colors.primary)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(exercise.name, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                    Text(exercise.scheme, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Text(
                exercise.tag,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
            exercise.stats.forEach { StatCell(it, Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun StatCell(stat: StrengthStat, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val valueColor = if (stat.muted) colors.onSurfaceVariant.copy(alpha = 0.5f) else colors.onSurface
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.2f), MaterialTheme.shapes.small)
            .padding(horizontal = RowGap, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            stat.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.5.sp,
            color = colors.onSurfaceVariant,
        )
        Text(stat.value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

// ── Cool-down (expandable) ────────────────────────────────────────────────────────────────────

@Composable
private fun CoolDownCard(state: TemplateStrengthDetailUiState, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val chevronRotation by animateFloatAsState(
        targetValue = if (state.coolDownExpanded) 90f else 0f,
        label = "coolDownChevron",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(CardPad),
            horizontalArrangement = Arrangement.spacedBy(CardPad),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphMedallion(CadenceIcons.CoolDown, tint = colors.onSurfaceVariant)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(state.coolDownTitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                Text(
                    "${state.coolDownItems.size} stretches",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Text(state.coolDownDuration, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Icon(
                CadenceIcons.ChevronRight,
                contentDescription = if (state.coolDownExpanded) "Collapse" else "Expand",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp).rotate(chevronRotation),
            )
        }
        if (state.coolDownExpanded) {
            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
            Column(
                modifier = Modifier.padding(CardPad),
                verticalArrangement = Arrangement.spacedBy(RowGap),
            ) {
                state.coolDownItems.forEach { CoolDownItemRow(it) }
            }
        }
    }
}

@Composable
private fun CoolDownItemRow(item: ProtocolItem) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.2f), MaterialTheme.shapes.small)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(CadenceIcons.Stretch, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Text(item.duration, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

// ── Shared pieces ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlyphMedallion(icon: ImageVector, tint: Color) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(Medallion)
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ── Top app bar ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onBack: () -> Unit, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassButton(CadenceIcons.ArrowBack, size = 16.dp, onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
            GlassButton(CadenceIcons.Share, size = 18.dp, onClick = {})
            GlassButton(CadenceIcons.MoreVert, size = 16.dp, onClick = onEdit)
        }
    }
}

@Composable
private fun GlassButton(icon: ImageVector, size: Dp, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colors.surfaceContainer.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(size))
    }
}

// ── Bottom CTA ─────────────────────────────────────────────────────────────────────────────────

@Composable
private fun StartCta(onStart: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.5f to colors.background,
                    1.0f to colors.background,
                ),
            )
            .padding(top = 40.dp, bottom = 20.dp, start = Gutter, end = Gutter),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClick = onStart),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(CadenceIcons.Play, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(11.dp, 14.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "Start Workout".uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = colors.onPrimary,
            )
        }
    }
}

// ── Glyph / image mapping (Android resources stay out of the shared state) ──────────────────────

private fun exerciseIcon(glyph: StrengthGlyph): ImageVector = when (glyph) {
    StrengthGlyph.BARBELL -> CadenceIcons.Barbell
    StrengthGlyph.DUMBBELL -> CadenceIcons.Dumbbell
    StrengthGlyph.MACHINE -> CadenceIcons.Dumbbell // no dedicated machine glyph yet; reuse the dumbbell
    StrengthGlyph.LOWER_BODY -> CadenceIcons.LowerBody
    StrengthGlyph.BODYWEIGHT -> CadenceIcons.LowerBody
}

private fun protocolIcon(glyph: BlockGlyph): ImageVector =
    if (glyph == BlockGlyph.WARM_UP) CadenceIcons.WarmUp else CadenceIcons.CoolDown

// All three currently share the interval-run photo as a placeholder until the real session photos land.
// When they do, switch to `when (session) { PUSH -> R.drawable.template_strength_push; ... }`.
@Suppress("UNUSED_PARAMETER")
private fun heroImageFor(session: StrengthSession): Int =
    when(session) {
        StrengthSession.PUSH -> R.drawable.template_hyrox_sim
        StrengthSession.PULL -> R.drawable.template_full_hyrox
        StrengthSession.LOWER -> R.drawable.template_lower_body
        else -> R.drawable.template_interval_run
        // Add more cases as needed
}

// ── Preview ──────────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TemplateStrengthDetailPreview() {
    CadenceTheme {
        TemplateStrengthDetailContent(
            state = TemplateStrengthDetailViewModel("upper-strength-a").uiState.value,
            onBack = {},
            onStart = {},
            onEdit = {},
            onToggleCoolDown = {},
        )
    }
}
