package dev.cadence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import dev.cadence.icons.Burpee
import dev.cadence.icons.Dumbbell
import dev.cadence.icons.Edit
import dev.cadence.icons.EngineRun
import dev.cadence.icons.Flag
import dev.cadence.icons.LowerBody
import dev.cadence.icons.Play
import dev.cadence.icons.Rowing
import dev.cadence.icons.SkiErg
import dev.cadence.icons.SledPull
import dev.cadence.icons.Timer
import dev.cadence.icons.WallBall
import dev.cadence.presentation.HyroxBlock
import dev.cadence.presentation.HyroxDivision
import dev.cadence.presentation.HyroxGlyph
import dev.cadence.presentation.HyroxRow
import dev.cadence.presentation.HyroxRowKind
import dev.cadence.presentation.HyroxVariant
import dev.cadence.presentation.TemplateHyroxDetailUiState
import dev.cadence.presentation.TemplateHyroxDetailViewModel
import dev.cadence.ui.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/*
 * Template Detail — HYROX sim (Figma 33:1727), shared by the Full and Half sims. A desaturated hero
 * (HYROX badge + duration + description), a division selector (+ a 1st/2nd/halved variant selector on
 * the half sim), a station/run block list (each block a functional station row over an interleaved run
 * row with a lime left-accent), a finish-line indicator, a SOLID translucent top app bar (back + title
 * + edit), and the sticky Start Workout CTA.
 *
 * Content is derived by [TemplateHyroxDetailViewModel] from the selected division + variant; layout is
 * stateless ([TemplateHyroxDetailContent]) so it previews and tests without Koin.
 */

private val Gutter = 20.dp
private val BarHeight = 64.dp

@Composable
fun TemplateHyroxDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: TemplateHyroxDetailViewModel = koinViewModel { parametersOf(templateId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CadenceTheme {
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
    val colors = MaterialTheme.colorScheme
    Scaffold(containerColor = colors.background) { inner ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + BarHeight,
                    bottom = inner.calculateBottomPadding() + 132.dp,
                ),
            ) {
                item(key = "hero", contentType = "hero") { HeroSection(state) }
                item(key = "config", contentType = "config") {
                    ConfigSection(
                        state = state,
                        onVariantSelected = onVariantSelected,
                        onDivisionSelected = onDivisionSelected,
                        modifier = Modifier.padding(top = 24.dp, start = Gutter, end = Gutter),
                    )
                }
                item(key = "list", contentType = "list") {
                    Column(
                        modifier = Modifier.padding(top = 24.dp, start = Gutter, end = Gutter),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                    ) {
                        state.blocks.forEach { BlockSection(it) }
                        FinishIndicator(state.finishLabel)
                    }
                }
            }

            TopBar(
                title = state.title,
                onBack = onBack,
                onEdit = onEdit,
                statusBarInset = inner.calculateTopPadding(),
                modifier = Modifier.align(Alignment.TopStart),
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
private fun HeroSection(state: TemplateHyroxDetailUiState) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(280.dp)) {
        Image(
            painter = painterResource(R.drawable.template_hyrox_sim),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.5f,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.5f to colors.background.copy(alpha = 0.4f),
                    1.0f to colors.background,
                ),
            ),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(Gutter),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        state.badge.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = colors.onPrimaryContainer,
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.surfaceContainerHigh.copy(alpha = 0.8f))
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                        .padding(horizontal = 13.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(CadenceIcons.Timer, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(13.dp))
                    Text(
                        state.duration,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 0.5.sp,
                        color = colors.onSurface,
                    )
                }
            }
            Text(
                state.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

// ── Config selectors ────────────────────────────────────────────────────────────────────────

@Composable
private fun ConfigSection(
    state: TemplateHyroxDetailUiState,
    onVariantSelected: (HyroxVariant) -> Unit,
    onDivisionSelected: (HyroxDivision) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.2.sp,
            color = colors.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

// ── Blocks ────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun BlockSection(block: HyroxBlock) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            block.label.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            letterSpacing = 1.6.sp,
            color = colors.primary,
            modifier = Modifier.padding(start = 4.dp),
        )
        block.rows.forEach { row ->
            if (row.kind == HyroxRowKind.STATION) StationRow(row) else RunRow(row)
        }
    }
}

@Composable
private fun StationRow(row: HyroxRow) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(17.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val style = hyroxGlyphStyle(row.glyph)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(colors.primaryContainer.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(style.width, style.height))
            }
            RowText(row.title, row.detail)
        }
        Text(row.value, style = MaterialTheme.typography.titleSmall, color = colors.primary)
    }
}

@Composable
private fun RunRow(row: HyroxRow) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lime left accent (rounded by the parent clip).
        Box(Modifier.width(4.dp).fillMaxHeight().background(colors.primary))
        Row(
            modifier = Modifier.weight(1f).padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val style = hyroxGlyphStyle(row.glyph)
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(style.width, style.height))
                }
                RowText(row.title, row.detail)
            }
            Text(row.value, style = MaterialTheme.typography.titleSmall, color = colors.primary)
        }
    }
}

@Composable
private fun RowText(title: String, detail: String) {
    val colors = MaterialTheme.colorScheme
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

/** Closes the flow after the final station: a flag medallion + "FINISH LINE" on the lime accent. */
@Composable
private fun FinishIndicator(label: String) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 33.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(CadenceIcons.Flag, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            }
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = colors.primary,
            )
        }
    }
}

// ── Top app bar (solid, translucent) ────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    statusBarInset: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background.copy(alpha = 0.8f)),
    ) {
        Spacer(Modifier.height(statusBarInset))
        Row(
            modifier = Modifier.fillMaxWidth().height(BarHeight).padding(horizontal = Gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BarButton(CadenceIcons.ArrowBack, size = 16.dp, onClick = onBack)
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
            }
            BarButton(CadenceIcons.Edit, size = 18.dp, onClick = onEdit)
        }
        HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant.copy(alpha = 0.2f))
    }
}

@Composable
private fun BarButton(icon: ImageVector, size: Dp, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
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
            .padding(top = 24.dp, bottom = 40.dp, start = Gutter, end = Gutter),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClick = onStart),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(CadenceIcons.Play, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(11.dp, 14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Start Workout",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.15.sp,
                color = colors.onPrimary,
            )
        }
    }
}

// ── Glyph mapping ───────────────────────────────────────────────────────────────────────────

private data class HyroxGlyphStyle(val icon: ImageVector, val tint: Color, val width: Dp, val height: Dp)

/** Station glyphs use the muted surfaceTint; the interleaved run figure uses the bright primary. */
@Composable
private fun hyroxGlyphStyle(glyph: HyroxGlyph): HyroxGlyphStyle {
    val c = MaterialTheme.colorScheme
    return when (glyph) {
        HyroxGlyph.SKI_ERG -> HyroxGlyphStyle(CadenceIcons.SkiErg, c.surfaceTint, 17.dp, 23.dp)
        HyroxGlyph.SLED_PUSH -> HyroxGlyphStyle(CadenceIcons.Dumbbell, c.surfaceTint, 20.dp, 20.dp)
        HyroxGlyph.SLED_PULL -> HyroxGlyphStyle(CadenceIcons.SledPull, c.surfaceTint, 16.dp, 18.dp)
        HyroxGlyph.BURPEE -> HyroxGlyphStyle(CadenceIcons.Burpee, c.surfaceTint, 22.dp, 22.dp)
        HyroxGlyph.ROWING -> HyroxGlyphStyle(CadenceIcons.Rowing, c.surfaceTint, 22.dp, 22.dp)
        HyroxGlyph.FARMERS_CARRY -> HyroxGlyphStyle(CadenceIcons.Barbell, c.surfaceTint, 20.dp, 20.dp)
        HyroxGlyph.SANDBAG_LUNGES -> HyroxGlyphStyle(CadenceIcons.LowerBody, c.surfaceTint, 16.dp, 22.dp)
        HyroxGlyph.WALL_BALLS -> HyroxGlyphStyle(CadenceIcons.WallBall, c.surfaceTint, 22.dp, 22.dp)
        HyroxGlyph.RUN -> HyroxGlyphStyle(CadenceIcons.EngineRun, c.primary, 18.dp, 22.dp)
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TemplateHyroxDetailPreview() {
    CadenceTheme {
        TemplateHyroxDetailContent(
            state = dev.cadence.presentation.TemplateHyroxDetailUiState(
                title = "Half Hyrox Sim",
                badge = "Hyrox",
                duration = "35-45 min",
                description = "The opening four stations at full race distance.",
                division = HyroxDivision.MEN,
                variant = HyroxVariant.FIRST_HALF,
                showVariantSelector = true,
                blocks = dev.cadence.presentation.HyroxStandards.buildBlocks(HyroxDivision.MEN, HyroxVariant.FIRST_HALF),
                finishLabel = "Finish Line",
            ),
            onBack = {},
            onStart = {},
            onEdit = {},
            onDivisionSelected = {},
            onVariantSelected = {},
        )
    }
}
