package com.mindset

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.icons.ArrowBack
import com.mindset.icons.Barbell
import com.mindset.icons.Dumbbell
import com.mindset.icons.EngineRun
import com.mindset.icons.LongRun
import com.mindset.icons.LowerBody
import com.mindset.icons.Stretch
import com.mindset.icons.Timer
import com.mindset.presentation.FeaturedTemplate
import com.mindset.presentation.LibraryTemplate
import com.mindset.presentation.TemplateCategory
import com.mindset.presentation.TemplateGlyph
import com.mindset.presentation.TemplateLibraryUiState
import com.mindset.presentation.TemplateLibraryViewModel
import com.mindset.ui.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TemplateLibraryScreen(
    onBack: () -> Unit,
    onNewTemplate: () -> Unit,
    onOpenTemplate: (String) -> Unit,
    viewModel: TemplateLibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
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
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TemplateLibraryTopBar(onBack = onBack) },
    ) { padding ->
        TemplateLibraryList(
            state = state,
            onOpenTemplate = onOpenTemplate,
            onCategorySelected = onCategorySelected,
            contentPadding = padding,
        )
    }
}

@Composable
private fun TemplateLibraryList(
    state: TemplateLibraryUiState,
    onOpenTemplate: (String) -> Unit,
    onCategorySelected: (TemplateCategory) -> Unit,
    contentPadding: PaddingValues,
) {
    val spacing = MaterialTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.md,
            end = spacing.md,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item(key = "chips", contentType = "chips") {
            CategoryChips(selected = state.selectedCategory, onSelected = onCategorySelected)
        }

        val featured = state.featured
        if (state.showClass && featured != null) {
            categorySection(key = "class", title = "Class Templates") {
                FeaturedCard(featured, onClick = { onOpenTemplate(featured.id) })
                state.classTemplates.forEach { template ->
                    ClassTemplateCard(template, onClick = { onOpenTemplate(template.id) })
                }
            }
        }

        if (state.showStrength) {
            categorySection(key = "strength", title = "Strength Blocks") {
                state.strengthBlocks.forEach { template ->
                    BlockCard(template, onClick = { onOpenTemplate(template.id) })
                }
            }
        }

        if (state.showEndurance) {
            categorySection(key = "endurance", title = "Endurance") {
                state.endurance.forEach { template ->
                    EnduranceCard(template, onClick = { onOpenTemplate(template.id) })
                }
            }
        }
    }
}

@Composable
private fun TemplateLibraryTopBar(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        title = { Text("Template Library") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    MindSetIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            scrolledContainerColor = colors.surface,
            titleContentColor = colors.onSurface,
        ),
    )
}

private fun LazyListScope.categorySection(
    key: String,
    title: String,
    action: String? = null,
    body: @Composable () -> Unit,
) {
    item(key = "section-$key", contentType = "section") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smd),
        ) {
            SectionHeader(title = title, action = action)
            body()
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String?) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun CategoryChips(
    selected: TemplateCategory,
    onSelected: (TemplateCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
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
        modifier = styled
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
        )
    }
}

@Composable
private fun cardShell(onClick: () -> Unit): Modifier {
    val colors = MaterialTheme.colorScheme
    return Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(colors.surfaceContainerLow)
        .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .padding(MaterialTheme.spacing.md)
}

@Composable
private fun FeaturedCard(featured: FeaturedTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    PhotoCard(
        painter = R.drawable.template_full_hyrox,
        height = 224.dp,
        onClick = onClick,
    ) {
        FlagBadge(featured.flag)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Text(
            featured.title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(
            featured.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            featured.tags.forEach { FloatingTag(it) }
        }
    }
}

@Composable
private fun ClassTemplateCard(template: LibraryTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        cardShell(onClick),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Medallion(template.glyph, size = 40.dp, bgAlpha = 0.2f)
            TagBadge(template.badge.name)
        }
        Text(template.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
        Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        if (template.pill != null) {
            Spacer(Modifier.height(MaterialTheme.spacing.smd))
            PillBadge(template.pill)
        }
    }
}

@Composable
private fun BlockCard(template: LibraryTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        cardShell(onClick),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BareGlyph(template.glyph)
            Text(template.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            TagBadge(template.badge.name)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xs))
        Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun EnduranceCard(template: LibraryTemplate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = cardShell(onClick),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Medallion(template.glyph, size = 48.dp, bgAlpha = 0.1f)
        Column(Modifier.weight(1f)) {
            Text(template.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        TagBadge(template.badge.name)
    }
}

/** Photo-backed hero card: cropped image, bottom scrim, bottom-anchored [content]. */
@Composable
private fun PhotoCard(
    painter: Int,
    height: Dp,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(painter),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(scrimBrush()))
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.md),
            content = content,
        )
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

/**
 * One implementation behind the four card tags. They differ only in shape, colours and how tight the
 * vertical padding is, so the named wrappers below stay one-liners and can't drift apart.
 */
@Composable
private fun Badge(text: String, shape: Shape, container: Color, contentColor: Color, vertical: Dp) {
    Box(
        modifier = Modifier
            .clip(shape)
            .background(container)
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = vertical),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

/** The rectangular tags sit tighter than [Spacing.xs]; 2dp is deliberate and has no token. */
private val TagVerticalPadding = 2.dp

/** Rectangular type tag (SIM / ELITE / STRENGTH / RUN / FLOW) on the surface-highest chip. */
@Composable
private fun TagBadge(text: String) = Badge(
    text = text,
    shape = MaterialTheme.shapes.extraSmall,
    container = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    vertical = TagVerticalPadding,
)

/** Pill-shaped tag on the surface-highest chip (class-sim bottom tag). */
@Composable
private fun PillBadge(text: String) = Badge(
    text = text,
    shape = CircleShape,
    container = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    vertical = MaterialTheme.spacing.xs,
)

/** Accent flag on the featured card ("OFFICIAL SIM"). */
@Composable
private fun FlagBadge(text: String) = Badge(
    text = text,
    shape = MaterialTheme.shapes.extraSmall,
    container = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    vertical = TagVerticalPadding,
)

/** Translucent tag over the featured photo. No live backdrop-blur (a per-frame cost); the 0.8 alpha
 *  surface reads the same at this size. */
@Composable
private fun FloatingTag(text: String) = Badge(
    text = text,
    shape = CircleShape,
    container = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
    contentColor = FloatingTagText,
    vertical = MaterialTheme.spacing.xs,
)

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
        Icon(
            style.icon,
            contentDescription = null,
            tint = style.tint,
            modifier = Modifier.size(style.width, style.height),
        )
    }
}

/** A bare tinted glyph with no medallion (strength block cards). */
@Composable
private fun BareGlyph(glyph: TemplateGlyph) {
    val style = glyphStyle(glyph)
    Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(style.width, style.height))
}

private data class GlyphStyle(val icon: ImageVector, val tint: Color, val medallion: Color, val width: Dp, val height: Dp)

/** Map a semantic glyph to its vector, tint, medallion base color, and intrinsic (aspect-preserving)
 *  display size. Sizes keep each exported glyph's aspect ratio so nothing is stretched. */
@Composable
private fun glyphStyle(glyph: TemplateGlyph): GlyphStyle {
    val c = MaterialTheme.colorScheme
    return when (glyph) {
        TemplateGlyph.STOPWATCH -> GlyphStyle(MindSetIcons.Timer, c.primary, c.primaryContainer, 19.dp, 22.dp)
        TemplateGlyph.ENGINE -> GlyphStyle(MindSetIcons.EngineRun, c.tertiary, c.tertiaryContainer, 18.dp, 22.dp)
        TemplateGlyph.DUMBBELL -> GlyphStyle(MindSetIcons.Dumbbell, c.primary, c.primaryContainer, 20.dp, 20.dp)
        TemplateGlyph.BARBELL -> GlyphStyle(MindSetIcons.Barbell, c.primary, c.primaryContainer, 20.dp, 20.dp)
        TemplateGlyph.LOWER_BODY -> GlyphStyle(MindSetIcons.LowerBody, c.primary, c.primaryContainer, 15.dp, 20.dp)
        TemplateGlyph.LONG_RUN -> GlyphStyle(MindSetIcons.LongRun, c.primary, c.primaryContainer, 20.dp, 20.dp)
        TemplateGlyph.TIMER -> GlyphStyle(MindSetIcons.Timer, c.primary, c.primaryContainer, 17.dp, 20.dp)
        TemplateGlyph.STRETCH -> GlyphStyle(MindSetIcons.Stretch, c.tertiary, c.tertiaryContainer, 22.dp, 20.dp)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TemplateLibraryPreview() {
    MindSetTheme {
        TemplateLibraryContent(
            state = TemplateLibraryUiState(),
            onBack = {},
            onNewTemplate = {},
            onOpenTemplate = {},
            onCategorySelected = {},
        )
    }
}
