package dev.cadence

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.icons.Add
import dev.cadence.icons.ArrowBack
import dev.cadence.icons.Barbell
import dev.cadence.icons.ChevronRight
import dev.cadence.icons.Dumbbell
import dev.cadence.icons.EngineRun
import dev.cadence.icons.Grid
import dev.cadence.icons.LongRun
import dev.cadence.icons.LowerBody
import dev.cadence.icons.Search
import dev.cadence.icons.Stretch
import dev.cadence.icons.Timer
import dev.cadence.icons.Tune
import dev.cadence.presentation.FeaturedTemplate
import dev.cadence.presentation.GridTemplate
import dev.cadence.presentation.LibraryTemplate
import dev.cadence.presentation.RecentTemplate
import dev.cadence.presentation.TemplateCategory
import dev.cadence.presentation.TemplateGlyph
import dev.cadence.presentation.TemplateLibraryUiState
import dev.cadence.presentation.TemplateLibraryViewModel
import dev.cadence.ui.R
import org.koin.compose.viewmodel.koinViewModel

/*
 * Template Library (Figma 33:1068) — the redesigned Templates surface. A single scrolling library of
 * curated + self-programmed templates grouped into four category sections, with a search field, a
 * category-chip filter, and a "Create Custom" action. Reached as a push from Home (back arrow, no
 * bottom nav — the mock's bottom bar is a design-file artifact for a pushed screen).
 *
 * The content is design-accurate sample data from a StateFlow (see [TemplateLibraryViewModel]); the
 * layout is stateless ([TemplateLibraryContent]) so it previews and tests without Koin.
 */

/** Edge gutter — 20dp like Home (not the 16dp grid), matching the Figma frame's px-20 margins. */
private val Gutter = 20.dp

@Composable
fun TemplateLibraryScreen(
    onBack: () -> Unit,
    onNewTemplate: () -> Unit,
    onOpenTemplate: (String) -> Unit,
    viewModel: TemplateLibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CadenceTheme {
        TemplateLibraryContent(
            state = state,
            onBack = onBack,
            onNewTemplate = onNewTemplate,
            onOpenTemplate = onOpenTemplate,
            onCategorySelected = viewModel::onCategorySelected,
        )
    }
}

@Composable
private fun TemplateLibraryContent(
    state: TemplateLibraryUiState,
    onBack: () -> Unit,
    onNewTemplate: () -> Unit,
    onOpenTemplate: (String) -> Unit,
    onCategorySelected: (TemplateCategory) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        floatingActionButton = { CreateCustomFab(onClick = onNewTemplate) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                // Clear the overlaid FAB so the last section is fully scrollable into view.
                bottom = padding.calculateBottomPadding() + 88.dp,
            ),
        ) {
            item(key = "top-bar", contentType = "top-bar") { TopBar(onBack = onBack) }
            item(key = "search", contentType = "search") { SearchField(Modifier.padding(top = 8.dp)) }
            item(key = "chips", contentType = "chips") {
                CategoryChips(
                    selected = state.selectedCategory,
                    onSelected = onCategorySelected,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            val featured = state.featured
            if (state.showClass && featured != null) {
                categorySection(
                    key = "class",
                    title = "Class Templates",
                    action = "See all",
                ) {
                    FeaturedCard(featured, onClick = { onOpenTemplate(featured.id) })
                    Spacer(Modifier.height(12.dp))
                    CardStack(state.classTemplates) { t ->
                        ClassTemplateCard(t, onClick = { onOpenTemplate(t.id) })
                    }
                }
            }

            if (state.showStrength) {
                categorySection(
                    key = "strength",
                    title = "Strength Blocks",
                    action = "Manage",
                ) {
                    CardStack(state.strengthBlocks) { t ->
                        BlockCard(t, onClick = { onOpenTemplate(t.id) })
                    }
                }
            }

            if (state.showEndurance) {
                categorySection(
                    key = "endurance",
                    title = "Endurance",
                ) {
                    CardStack(state.endurance) { t ->
                        EnduranceCard(t, onClick = { onOpenTemplate(t.id) })
                    }
                }
            }

            if (state.showSelfProgrammed) {
                categorySection(
                    key = "self",
                    title = "Self-Programmed",
                    action = "Manage",
                ) {
                    state.recent?.let { LargeFeatureCard(it, onClick = { onOpenTemplate(it.id) }) }
                    if (state.recent != null && state.grid != null) Spacer(Modifier.height(12.dp))
                    state.grid?.let { GridCard(it, onClick = { onOpenTemplate(it.id) }) }
                }
            }
        }
    }
}

// ── Section scaffold ────────────────────────────────────────────────────────────────────────────

/** A LazyList item holding one titled category section (header + 40dp top gap + gutter-padded body). */
private fun androidx.compose.foundation.lazy.LazyListScope.categorySection(
    key: String,
    title: String,
    action: String? = null,
    body: @Composable () -> Unit,
) {
    item(key = "section-$key", contentType = "section") {
        Column(Modifier.fillMaxWidth().padding(top = 40.dp)) {
            SectionHeader(title = title, action = action)
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = Gutter)) { body() }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String?) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.7.sp,
            color = colors.onSurfaceVariant,
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = colors.primary,
            )
        }
    }
}

/** Stack a list of cards with the standard 12dp inter-card gap. */
@Composable
private fun <T> CardStack(items: List<T>, card: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { card(it) }
    }
}

// ── Top bar / search / chips ──────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton48(icon = CadenceIcons.ArrowBack, tint = colors.onSurface, size = 16.dp, onClick = onBack)
            Spacer(Modifier.size(4.dp))
            Text(
                text = "Template Library",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
            )
        }
        IconButton48(icon = CadenceIcons.Tune, tint = colors.onSurface, size = 18.dp, onClick = {})
    }
}

/** 48dp circular touch target with a centered glyph (top-app-bar buttons). */
@Composable
private fun IconButton48(icon: ImageVector, tint: Color, size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
    }
}

@Composable
private fun SearchField(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter)
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerHigh)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(CadenceIcons.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(
            text = "Search name, muscle, equipment",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun CategoryChips(
    selected: TemplateCategory,
    onSelected: (TemplateCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TemplateCategory.entries.forEach { category ->
            CategoryChip(
                label = category.label,
                selected = category == selected,
                onClick = { onSelected(category) },
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val base = Modifier.clip(CircleShape)
    val styled = if (selected) {
        base.background(colors.primaryContainer)
    } else {
        base.background(colors.surfaceContainer).border(1.dp, colors.outlineVariant, CircleShape)
    }
    Box(
        modifier = styled.clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
        )
    }
}

// ── Cards ───────────────────────────────────────────────────────────────────────────────────────

/** Shared shell for the bordered detail cards: rounded, low tonal surface, hairline outline. */
@Composable
private fun cardShell(onClick: () -> Unit): Modifier {
    val colors = MaterialTheme.colorScheme
    return Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(colors.surfaceContainerLow)
        .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .padding(17.dp)
}

@Composable
private fun FeaturedCard(featured: FeaturedTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.template_full_hyrox),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(scrimBrush()))
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp)) {
            FlagBadge(featured.flag)
            Spacer(Modifier.height(8.dp))
            Text(
                text = featured.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = featured.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                featured.tags.forEach { FloatingTag(it) }
            }
        }
    }
}

@Composable
private fun ClassTemplateCard(template: LibraryTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(cardShell(onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Medallion(template.glyph, size = 40.dp, bgAlpha = 0.2f)
            TagBadge(template.badge.name)
        }
        Spacer(Modifier.height(12.dp))
        Text(template.title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        if (template.pill != null) {
            Spacer(Modifier.height(16.dp))
            PillBadge(template.pill)
        }
    }
}

@Composable
private fun BlockCard(template: LibraryTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(cardShell(onClick), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BareGlyph(template.glyph)
            TagBadge(template.badge.name)
        }
        Text(
            template.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun EnduranceCard(template: LibraryTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = cardShell(onClick),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Medallion(template.glyph, size = 48.dp, bgAlpha = 0.1f)
        Column(Modifier.weight(1f)) {
            Text(template.title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        TagBadge(template.badge.name)
    }
}

@Composable
private fun LargeFeatureCard(recent: RecentTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.template_heavy_squat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(scrimBrush()))
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(colors.primaryContainer))
                Text(
                    text = "MOST RECENT",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.5.sp,
                    color = colors.primaryContainer,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(recent.title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(recent.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun GridCard(grid: GridTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(17.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(CadenceIcons.Grid, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp, 16.dp))
            Icon(CadenceIcons.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(7.4.dp, 12.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(grid.title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        Spacer(Modifier.height(16.dp))
        Text(grid.meta.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 0.5.sp, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun CreateCustomFab(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .shadow(6.dp, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(CadenceIcons.Add, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(24.dp))
        Text("Create Custom", style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
    }
}

// ── Small shared pieces ──────────────────────────────────────────────────────────────────────────

/** Bottom-anchored image scrim: transparent at the top → deepest surface at the bottom. */
@Composable
private fun scrimBrush(): Brush {
    val deep = MaterialTheme.colorScheme.surfaceContainerLowest
    return Brush.verticalGradient(
        0.0f to Color.Transparent,
        0.5f to deep.copy(alpha = 0.4f),
        1.0f to deep,
    )
}

/** Rectangular type tag (SIM / ELITE / STRENGTH / RUN / FLOW) on the surface-highest chip. */
@Composable
private fun TagBadge(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(colors.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 0.5.sp, color = colors.onSurfaceVariant)
    }
}

/** Pill-shaped tag on the surface-highest chip (class-sim bottom tag). */
@Composable
private fun PillBadge(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 0.5.sp, color = colors.onSurfaceVariant)
    }
}

/** Accent flag on the featured card ("OFFICIAL SIM"). */
@Composable
private fun FlagBadge(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(colors.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 0.5.sp, color = colors.onPrimaryContainer)
    }
}

/** Translucent tag over the featured photo. No live backdrop-blur (a per-frame cost); the 0.8 alpha
 *  surface reads the same at this size. */
@Composable
private fun FloatingTag(text: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.surfaceContainerHigh.copy(alpha = 0.8f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 0.5.sp, color = FloatingTagText)
    }
}

private val FloatingTagText = Color(0xFFCBEF97)

/** Faint rounded medallion holding a tinted glyph (class-sim + endurance leading icon). */
@Composable
private fun Medallion(glyph: TemplateGlyph, size: Dp, bgAlpha: Float) {
    val style = glyphStyle(glyph)
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(style.medallion.copy(alpha = bgAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(style.width, style.height))
    }
}

/** A bare tinted glyph with no medallion (strength block cards). */
@Composable
private fun BareGlyph(glyph: TemplateGlyph) {
    val style = glyphStyle(glyph)
    Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(style.width, style.height))
}

private data class GlyphStyle(
    val icon: ImageVector,
    val tint: Color,
    val medallion: Color,
    val width: Dp,
    val height: Dp,
)

/** Map a semantic glyph to its vector, tint, medallion base color, and intrinsic (aspect-preserving)
 *  display size. Sizes keep each exported glyph's aspect ratio so nothing is stretched. */
@Composable
private fun glyphStyle(glyph: TemplateGlyph): GlyphStyle {
    val c = MaterialTheme.colorScheme
    return when (glyph) {
        TemplateGlyph.STOPWATCH -> GlyphStyle(CadenceIcons.Timer, c.primary, c.primaryContainer, 19.dp, 22.dp)
        TemplateGlyph.ENGINE -> GlyphStyle(CadenceIcons.EngineRun, c.tertiary, c.tertiaryContainer, 18.dp, 22.dp)
        TemplateGlyph.DUMBBELL -> GlyphStyle(CadenceIcons.Dumbbell, c.primary, c.primaryContainer, 20.dp, 20.dp)
        TemplateGlyph.BARBELL -> GlyphStyle(CadenceIcons.Barbell, c.primary, c.primaryContainer, 20.dp, 20.dp)
        TemplateGlyph.LOWER_BODY -> GlyphStyle(CadenceIcons.LowerBody, c.primary, c.primaryContainer, 15.dp, 20.dp)
        TemplateGlyph.LONG_RUN -> GlyphStyle(CadenceIcons.LongRun, c.primary, c.primaryContainer, 20.dp, 20.dp)
        TemplateGlyph.TIMER -> GlyphStyle(CadenceIcons.Timer, c.primary, c.primaryContainer, 17.dp, 20.dp)
        TemplateGlyph.STRETCH -> GlyphStyle(CadenceIcons.Stretch, c.tertiary, c.tertiaryContainer, 22.dp, 20.dp)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TemplateLibraryPreview() {
    CadenceTheme {
        TemplateLibraryContent(
            state = TemplateLibraryUiState(),
            onBack = {},
            onNewTemplate = {},
            onOpenTemplate = {},
            onCategorySelected = {},
        )
    }
}
