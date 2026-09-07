package com.mindset

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.icons.ArrowBack
import com.mindset.icons.Bolt
import com.mindset.icons.CoolDown
import com.mindset.icons.Flame
import com.mindset.icons.MoreVert
import com.mindset.icons.NavAccount
import com.mindset.icons.Play
import com.mindset.icons.Share
import com.mindset.icons.Timer
import com.mindset.icons.WarmUp
import com.mindset.presentation.BlockGlyph
import com.mindset.presentation.DetailTag
import com.mindset.presentation.ProtocolBlock
import com.mindset.presentation.TemplateDetailUiState
import com.mindset.presentation.TemplateDetailViewModel
import com.mindset.presentation.WorkSegment
import com.mindset.ui.R
import org.koin.compose.viewmodel.koinViewModel

/*
 * Template Detail (Figma 33:1455 — "Interval Run"). A full-bleed hero photo with an overlaid,
 * translucent floating app bar, a "Workout Protocol" list (warm-up / highlighted work segment /
 * cool-down), a coach's-notes card, and a pinned START WORKOUT CTA. Reached as a push from the
 * Template Library (back arrow, no bottom nav).
 *
 * Design-accurate static content from [TemplateDetailViewModel]; layout is stateless
 * ([TemplateDetailContent]) so it previews and tests without Koin.
 */

private val Gutter = 20.dp

@Composable
fun TemplateDetailScreen(onBack: () -> Unit, onStart: () -> Unit, onEdit: () -> Unit, viewModel: TemplateDetailViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        TemplateDetailContent(state = state, onBack = onBack, onStart = onStart, onEdit = onEdit)
    }
}

@Composable
private fun TemplateDetailContent(state: TemplateDetailUiState, onBack: () -> Unit, onStart: () -> Unit, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Scaffold(containerColor = colors.background) { inner ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Hero bleeds to the very top (under the status bar); leave room at the bottom for the CTA.
                contentPadding = PaddingValues(bottom = inner.calculateBottomPadding() + 132.dp),
            ) {
                item(key = "hero", contentType = "hero") { HeroHeader(state) }
                item(key = "body", contentType = "body") {
                    Column(
                        modifier = Modifier.padding(top = 24.dp, start = Gutter, end = Gutter),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ProtocolHeader(state.protocolCount)
                        SimpleBlock(state.warmUp)
                        WorkBlock(state.work)
                        SimpleBlock(state.coolDown)
                        CoachNotes(state.coachNote, state.coachName, Modifier.padding(top = 16.dp))
                    }
                }
            }

            // Floating translucent top app bar (below the status bar).
            TopBar(
                onBack = onBack,
                onEdit = onEdit,
                modifier = Modifier.align(Alignment.TopStart).padding(top = inner.calculateTopPadding()),
            )

            // Pinned bottom CTA with a fade-to-background scrim.
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
private fun HeroHeader(state: TemplateDetailUiState) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(340.dp)) {
        Image(
            painter = painterResource(R.drawable.template_interval_run),
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.tags.forEach { HeroTag(it) }
            }
            Text(
                text = state.title,
                style = MaterialTheme.typography.bodyLarge,
                letterSpacing = (-0.4).sp,
                color = colors.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                MetaStat(MindSetIcons.Timer, state.duration, 15.dp)
                MetaStat(MindSetIcons.Flame, state.calories, 14.dp)
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
        Text(tag.text.uppercase(), style = MaterialTheme.typography.bodyLarge, color = text)
    }
}

@Composable
private fun MetaStat(icon: ImageVector, label: String, iconSize: androidx.compose.ui.unit.Dp) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(iconSize))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
    }
}

// ── Top app bar ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onBack: () -> Unit, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassButton(MindSetIcons.ArrowBack, size = 16.dp, onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassButton(MindSetIcons.Share, size = 18.dp, onClick = {})
            GlassButton(MindSetIcons.MoreVert, size = 16.dp, onClick = onEdit)
        }
    }
}

/** 48dp translucent circular button (a flat fill stands in for the design's live backdrop-blur). */
@Composable
private fun GlassButton(icon: ImageVector, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
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

// ── Protocol ──────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProtocolHeader(count: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Workout Protocol".uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            letterSpacing = 1.6.sp,
            color = colors.onSurfaceVariant,
        )
        Text(count.uppercase(), style = MaterialTheme.typography.bodyLarge, color = colors.primary)
    }
}

@Composable
private fun SimpleBlock(block: ProtocolBlock) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(17.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(colors.surfaceContainerHigh)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            val icon = if (block.glyph == BlockGlyph.WARM_UP) MindSetIcons.WarmUp else MindSetIcons.CoolDown
            Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(block.title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                Text(block.duration, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
            Text(block.detail, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun WorkBlock(work: WorkSegment) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.primary.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
    ) {
        // Header strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary.copy(alpha = 0.05f))
                .padding(top = 10.dp, bottom = 11.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    MindSetIcons.Bolt,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(13.dp, 17.dp),
                )
                Text(
                    work.label.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                )
            }
            Text(
                work.repeats.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.primary.copy(alpha = 0.2f))

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Work sub-block
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AccentBar(height = 64.dp, color = colors.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(work.exercise, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        work.stats.forEach { StatCard(it.label, it.value, Modifier.weight(1f)) }
                    }
                }
            }
            // Recovery sub-block
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AccentBar(height = 40.dp, color = colors.outlineVariant)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            work.recoveryTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            work.recoveryDuration,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        work.recoveryDetail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentBar(height: androidx.compose.ui.unit.Dp, color: Color) {
    Box(Modifier.width(6.dp).height(height).clip(CircleShape).background(color))
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.2f), MaterialTheme.shapes.small)
            .padding(13.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Text(value, fontSize = 18.sp, lineHeight = 28.sp, color = colors.onPrimaryContainer)
    }
}

// ── Coach notes ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoachNotes(note: String, coach: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Coach's Notes".uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            letterSpacing = 1.6.sp,
            color = colors.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerLow)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
                .padding(21.dp),
        ) {
            // Faint decorative quotation mark, top-right.
            Text(
                "”",
                fontSize = 72.sp,
                color = colors.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.align(Alignment.TopEnd),
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 26.sp,
                    color = colors.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            NavAccount,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(coach, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }
        }
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
            Icon(
                MindSetIcons.Play,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(11.dp, 14.dp),
            )
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

// ── Preview ──────────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TemplateDetailPreview() {
    MindSetTheme {
        TemplateDetailContent(
            state = TemplateDetailViewModel().uiState.value,
            onBack = {},
            onStart = {},
            onEdit = {},
        )
    }
}
