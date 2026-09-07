package com.mindset

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.FieldLabel
import com.mindset.components.HeaderIconButton
import com.mindset.components.PrimaryButton
import com.mindset.icons.ArrowBack
import com.mindset.icons.Burpee
import com.mindset.icons.Dumbbell
// TODO(phase2): restore with the hero's edit pencil below.
// import com.mindset.icons.Edit
import com.mindset.icons.Flag
import com.mindset.icons.LowerBody
import com.mindset.icons.Play
import com.mindset.icons.Rowing
import com.mindset.icons.Run
import com.mindset.icons.SkiErg
import com.mindset.icons.SledPull
import com.mindset.icons.Timer
import com.mindset.icons.WallBall
import com.mindset.presentation.HyroxBlock
import com.mindset.presentation.HyroxDivision
import com.mindset.presentation.HyroxGlyph
import com.mindset.presentation.HyroxRow
import com.mindset.presentation.HyroxRowKind
import com.mindset.presentation.HyroxStandards
import com.mindset.presentation.HyroxVariant
import com.mindset.presentation.TemplateHyroxDetailUiState
import com.mindset.presentation.TemplateHyroxDetailViewModel
import com.mindset.ui.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TemplateHyroxDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: TemplateHyroxDetailViewModel = koinViewModel { parametersOf(templateId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        TemplateHyroxDetailContent(
            state = state,
            onBack = onBack,
            // Start hands off to the app-scoped timer; the ActiveWorkoutHost overlay animates up.
            onStart = viewModel::startWorkout,
            onEdit = onEdit,
            onDivisionSelected = viewModel::onDivisionSelected,
            onVariantSelected = viewModel::onVariantSelected,
        )
    }
}

@Composable
private fun TemplateHyroxDetailContent(
    state: TemplateHyroxDetailUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDivisionSelected: (HyroxDivision) -> Unit,
    onVariantSelected: (HyroxVariant) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.background,
    ) { inner ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding(),
                    // The CTA floats over the list, so the last row has to clear it.
                    bottom = inner.calculateBottomPadding() + CtaReservedHeight,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                item(key = "hero") { Hero(state) }
                item(key = "config") {
                    ConfigSection(
                        state = state,
                        onVariantSelected = onVariantSelected,
                        onDivisionSelected = onDivisionSelected,
                        modifier = Modifier.padding(horizontal = spacing.md),
                    )
                }
                item(key = "blocks") {
                    Column(
                        modifier = Modifier.padding(horizontal = spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.xl),
                    ) {
                        state.blocks.forEach { BlockSection(it) }
                        FinishIndicator(state.finishLabel)
                    }
                }
            }

            // Back floats over the hero rather than scrolling away inside it: the screen carries no
            // toolbar, so once the list has scrolled past the photograph this is the only affordance
            // left besides the system gesture.
            HeaderIconButton(
                icon = MindSetIcons.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = inner.calculateTopPadding())
                    .padding(spacing.md),
                container = translucentOverPhoto(),
            )
            // TODO(phase2): re-enable, cut from v1 scope — editing needs TemplateBuilder, whose route
            // is not registered in v1, so the pencil would navigate nowhere.
//            HeaderIconButton(
//                icon = MindSetIcons.Edit,
//                contentDescription = "Edit template",
//                onClick = onEdit,
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(top = inner.calculateTopPadding())
//                    .padding(spacing.md),
//                container = translucentOverPhoto(),
//            )

            StartCta(
                blockedReason = state.startBlockedReason,
                onStart = onStart,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = inner.calculateBottomPadding()),
            )
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────────────────────

private val HeroHeight = 280.dp

/**
 * The photographic header, styled to match the Home rail card this screen opens from: same
 * photograph, same bottom-anchored scrim, same badge shapes.
 */
@Composable
private fun Hero(state: TemplateHyroxDetailUiState) {
    val colors = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    Box(Modifier.fillMaxWidth().height(HeroHeight)) {
        Image(
            painter = painterResource(R.drawable.template_hyrox_sim),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Ends in the page background (not the rail card's deeper surface) so the photograph
        // dissolves into the list rather than banding against it.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.5f to colors.background.copy(alpha = ScrimMidAlpha),
                    1.0f to colors.background,
                ),
            ),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FormatBadge(state.badge)
                DurationTag(state.duration)
            }
            Text(
                state.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                state.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

/** Names the format ("HYROX") — the rail card's flag badge at detail scale. */
@Composable
private fun FormatBadge(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(colors.primaryContainer)
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onPrimaryContainer,
        )
    }
}

/** Estimated duration, on the rail card's translucent tag. */
@Composable
private fun DurationTag(text: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(translucentOverPhoto())
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            MindSetIcons.Timer,
            contentDescription = null,
            tint = colors.onSurface,
            modifier = Modifier.size(TagIconSize),
        )
        Text(text, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
    }
}

/**
 * Fill for chrome sitting on the photograph. No live backdrop-blur — that is a per-frame GPU cost, and
 * at this size the flat translucent surface reads the same. Same trade the Home rail card makes.
 */
@Composable
private fun translucentOverPhoto(): Color =
    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = TranslucentAlpha)

private const val TranslucentAlpha = 0.8f
private const val ScrimMidAlpha = 0.4f
private val TagIconSize = 12.dp

// ── Config selectors ────────────────────────────────────────────────────────────────────────

@Composable
private fun ConfigSection(
    state: TemplateHyroxDetailUiState,
    onVariantSelected: (HyroxVariant) -> Unit,
    onDivisionSelected: (HyroxDivision) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        if (state.showVariantSelector) {
            SelectorGroup(
                label = "Simulation",
                options = HYROX_VARIANTS,
                selected = state.variant,
                labelOf = { it.label },
                onSelect = onVariantSelected,
            )
        }
        SelectorGroup(
            label = "Division",
            options = HYROX_DIVISIONS,
            selected = state.division,
            labelOf = { it.label },
            onSelect = onDivisionSelected,
        )
    }
}

// Stable option lists (avoid rebuilding on each recomposition).
private val HYROX_VARIANTS = listOf(HyroxVariant.FIRST_HALF, HyroxVariant.SECOND_HALF, HyroxVariant.HALVED)
private val HYROX_DIVISIONS = HyroxDivision.entries.toList()

@Composable
private fun <T> SelectorGroup(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column {
        FieldLabel(label)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            options.forEach { option ->
                SelectorChip(
                    label = labelOf(option),
                    selected = option == selected,
                    onClick = { onSelect(option) },
                )
            }
        }
    }
}

/** Single-choice pill: the brand fill when picked, otherwise the app's glass surface. */
@Composable
private fun SelectorChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val fill = if (selected) {
        Modifier.clip(CircleShape).background(colors.primary)
    } else {
        Modifier.glassSurface(CircleShape)
    }
    Box(
        modifier = fill
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) colors.onPrimary else colors.onSurface,
        )
    }
}

// ── Blocks ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun BlockSection(block: HyroxBlock) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd)) {
        MindSetSectionHeader(block.label, Modifier.fillMaxWidth())
        block.rows.forEach { row ->
            if (row.kind == HyroxRowKind.STATION) StationRow(row) else RunRow(row)
        }
    }
}

/** A functional station: the app's glass card, with the station glyph on a filled medallion. */
@Composable
private fun StationRow(row: HyroxRow) {
    RowBody(
        row = row,
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface()
            .padding(MaterialTheme.spacing.md),
    )
}

/**
 * The 1km run INTO the next station. Deliberately lighter than a station — a filled, borderless card
 * rather than glass — and carries the brand accent down its leading edge, so the race's run/station
 * alternation is legible while scrolling. Same weighting as the History timeline.
 */
@Composable
private fun RunRow(row: HyroxRow) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(colors.surfaceContainerLow),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Rounded by the parent clip.
        Box(Modifier.width(RunAccentWidth).fillMaxHeight().background(colors.primary))
        RowBody(
            row = row,
            modifier = Modifier.weight(1f).padding(MaterialTheme.spacing.md),
        )
    }
}

private val RunAccentWidth = 4.dp

/** Shared row content — glyph, title/detail, target value — so the two row weights can't drift apart. */
@Composable
private fun RowBody(row: HyroxRow, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphMedallion(row.glyph, row.kind)
        Column(Modifier.weight(1f)) {
            Text(
                row.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                row.detail,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
        Text(
            row.value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
    }
}

/**
 * Leading glyph. A station gets the filled medallion (the sheet-row treatment from Log Session); a run
 * keeps the same footprint unfilled, so titles stay on one vertical line down the whole block.
 */
@Composable
private fun GlyphMedallion(glyph: HyroxGlyph, kind: HyroxRowKind) {
    val colors = MaterialTheme.colorScheme
    val isStation = kind == HyroxRowKind.STATION
    Box(
        modifier = Modifier
            .size(MedallionSize)
            .clip(CircleShape)
            .then(if (isStation) Modifier.background(colors.surfaceContainerHigh) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            glyph.icon(),
            contentDescription = null,
            tint = if (isStation) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(GlyphSize),
        )
    }
}

private val MedallionSize = 36.dp
private val GlyphSize = 16.dp

/** Closes the flow after the final station: a flag medallion + "FINISH LINE" on the brand accent. */
@Composable
private fun FinishIndicator(label: String) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = GlassBorder)
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
        ) {
            Box(
                modifier = Modifier
                    .size(FinishMedallionSize)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = FinishMedallionAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    MindSetIcons.Flag,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(FinishIconSize),
                )
            }
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
        }
    }
}

private val FinishMedallionSize = 48.dp
private val FinishIconSize = 20.dp
private const val FinishMedallionAlpha = 0.12f

// ── Bottom CTA ─────────────────────────────────────────────────────────────────────────────────

/** Space the floating CTA takes out of the list: the scrim's top fade + the button + its bottom inset. */
private val CtaReservedHeight = 120.dp

/**
 * The sticky primary action. [blockedReason] non-null means a race is already live app-wide — one
 * timer at a time — so the button names what is running instead of offering a start that the
 * controller would refuse. Getting back to that race is the floating live pill, which
 * `ActiveWorkoutHost` renders top-right on exactly the toolbar-less screens like this one.
 */
@Composable
private fun StartCta(blockedReason: String?, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Fades the list out under the button rather than cutting it off with a hard edge.
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.5f to colors.background,
                    1.0f to colors.background,
                ),
            )
            .padding(
                start = MaterialTheme.spacing.md,
                end = MaterialTheme.spacing.md,
                top = MaterialTheme.spacing.xl,
                bottom = MaterialTheme.spacing.md,
            ),
    ) {
        PrimaryButton(
            text = blockedReason ?: "Start Workout",
            enabled = blockedReason == null,
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = if (blockedReason == null) MindSetIcons.Play else MindSetIcons.Timer,
        )
    }
}

// ── Glyph mapping ───────────────────────────────────────────────────────────────────────────

/**
 * Station glyph → icon. Mirrors [com.mindset.helpers.UIHelper]'s mapping, which keys off the domain
 * station/segment instead; this screen's rows carry the presentation-side [HyroxGlyph], so the lookup
 * lives here rather than pulling a feature enum into `:core:ui`.
 */
private fun HyroxGlyph.icon(): ImageVector = when (this) {
    HyroxGlyph.SKI_ERG -> MindSetIcons.SkiErg
    HyroxGlyph.SLED_PUSH -> MindSetIcons.SledPull
    HyroxGlyph.SLED_PULL -> MindSetIcons.SledPull
    HyroxGlyph.BURPEE -> MindSetIcons.Burpee
    HyroxGlyph.ROWING -> MindSetIcons.Rowing
    HyroxGlyph.FARMERS_CARRY -> MindSetIcons.Dumbbell
    HyroxGlyph.SANDBAG_LUNGES -> MindSetIcons.LowerBody
    HyroxGlyph.WALL_BALLS -> MindSetIcons.WallBall
    HyroxGlyph.RUN -> MindSetIcons.Run
}

// ── Preview ──────────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TemplateHyroxDetailPreview() {
    MindSetTheme {
        TemplateHyroxDetailContent(
            state = previewState(),
            onBack = {},
            onStart = {},
            onEdit = {},
            onDivisionSelected = {},
            onVariantSelected = {},
        )
    }
}

/** The CTA's blocked state: a race is already live, so Start names it instead of offering a start. */
@Preview
@Composable
private fun TemplateHyroxDetailRaceLivePreview() {
    MindSetTheme {
        TemplateHyroxDetailContent(
            state = previewState().copy(startBlockedReason = "Full simulation in progress"),
            onBack = {},
            onStart = {},
            onEdit = {},
            onDivisionSelected = {},
            onVariantSelected = {},
        )
    }
}

private fun previewState(): TemplateHyroxDetailUiState =
    TemplateHyroxDetailUiState(
        title = "Half Hyrox Sim",
        badge = "Hyrox",
        duration = "35-45 min",
        description = "The opening four stations at full race distance.",
        division = HyroxDivision.MEN,
        variant = HyroxVariant.FIRST_HALF,
        showVariantSelector = true,
        blocks = HyroxStandards.buildBlocks(HyroxDivision.MEN, HyroxVariant.FIRST_HALF),
        finishLabel = "Finish Line",
    )
