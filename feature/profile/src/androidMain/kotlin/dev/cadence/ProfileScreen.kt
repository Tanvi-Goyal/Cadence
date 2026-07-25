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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.icons.ChevronRight
import dev.cadence.icons.DarkMode
import dev.cadence.icons.Database
import dev.cadence.icons.Delete
import dev.cadence.icons.Download
import dev.cadence.icons.Edit
import dev.cadence.icons.Menu
import dev.cadence.icons.NavAccount
import dev.cadence.icons.Ruler
import dev.cadence.icons.Shield
import dev.cadence.icons.Watch
import dev.cadence.domain.ThemeMode
import dev.cadence.domain.WeightUnit
import dev.cadence.presentation.PreferencesViewModel
import org.koin.compose.viewmodel.koinViewModel

/*
 * Profile / Settings (Figma "Profile"). A profile header (avatar + name + tier), a 3-stat row, and
 * three grouped setting sections (Preferences / Integrations / Account & Security) over a version
 * footer, on the Kinetic Precision dark theme.
 *
 * Two settings are real (wired to [PreferencesViewModel]): the Dark Mode toggle → themeMode and the
 * Units row → weightUnit. Everything else (avatar/name/tier, stats, integrations, account actions) is
 * design-static placeholder content, to be wired to real session stats / integrations in a later pass.
 */

private val Gutter = 20.dp

@Composable
fun ProfileScreen(
    onTab: (Tab) -> Unit,
    onOpenCredits: () -> Unit,
    viewModel: PreferencesViewModel = koinViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    CadenceTheme {
        val colors = MaterialTheme.colorScheme
        // Health Connect has no backend yet — a local, visual-only toggle.
        var healthConnected by remember { mutableStateOf(true) }

        Scaffold(
            containerColor = colors.background,
            bottomBar = { CadenceBottomBar(current = Tab.Profile, onTab = onTab) },
        ) { inner ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding(),
                    bottom = inner.calculateBottomPadding() + 24.dp,
                ),
            ) {
                item {
                    Column {
                        TopBar()
                        Spacer(Modifier.height(8.dp))
                        HeaderCard(name = "Alex Thorne", tier = "Elite Hybrid • Level 42")
                        Spacer(Modifier.height(16.dp))
                        StatsRow()
                        Spacer(Modifier.height(28.dp))

                        SettingsSection("Preferences") {
                            ToggleRow(
                                icon = CadenceIcons.DarkMode,
                                title = "Dark Mode",
                                subtitle = "Optimized for high-intensity focus",
                                checked = prefs.themeMode != ThemeMode.LIGHT,
                                onCheckedChange = { on ->
                                    // Persisted, though the app renders dark-only until a light scheme lands.
                                    viewModel.onThemeModeChange(if (on) ThemeMode.DARK else ThemeMode.LIGHT)
                                },
                            )
                            RowDivider()
                            NavRow(
                                icon = CadenceIcons.Ruler,
                                title = "Units",
                                subtitle = if (prefs.weightUnit == WeightUnit.KG) "Metric (kg)" else "Imperial (lb)",
                                onClick = {
                                    viewModel.onWeightUnitChange(
                                        if (prefs.weightUnit == WeightUnit.KG) WeightUnit.LB else WeightUnit.KG,
                                    )
                                },
                            )
                        }
                        Spacer(Modifier.height(24.dp))

                        SettingsSection("Integrations") {
                            ToggleRow(
                                icon = CadenceIcons.Shield,
                                iconTint = colors.primary,
                                title = "Health Connect",
                                subtitle = "Sync calories and heart rate",
                                checked = healthConnected,
                                onCheckedChange = { healthConnected = it },
                            )
                            RowDivider()
                            NavRow(
                                icon = CadenceIcons.Watch,
                                title = "Wearable Devices",
                                subtitle = "2 Connected devices",
                                onClick = {},
                            )
                        }
                        Spacer(Modifier.height(24.dp))

                        SettingsSection("Account & Security") {
                            NavRow(
                                icon = CadenceIcons.Sync,
                                title = "Cloud Sync",
                                subtitle = "Last synced: 2 minutes ago",
                                onClick = {},
                            )
                            RowDivider()
                            SettingRow(
                                icon = CadenceIcons.Database,
                                title = "Data Export",
                                subtitle = "JSON, CSV, and FIT formats",
                                onClick = {},
                                trailing = {
                                    Icon(
                                        CadenceIcons.Download,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                            )
                            RowDivider()
                            SettingRow(
                                icon = CadenceIcons.Delete,
                                iconTint = colors.error,
                                title = "Delete Account",
                                titleColor = colors.error,
                                subtitle = "Permanently remove all data",
                                onClick = {},
                                trailing = { ChevronTrailing() },
                            )
                        }
                        Spacer(Modifier.height(32.dp))
                        Footer(onOpenCredits = onOpenCredits)
                    }
                }
            }
        }
    }
}

// ── Top bar ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar() {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp)) {
        IconButton48(CadenceIcons.Menu, size = 20.dp, modifier = Modifier.align(Alignment.CenterStart)) {}
        Text(
            "Profile",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            modifier = Modifier.align(Alignment.Center),
        )
        IconButton48(CadenceIcons.Edit, size = 18.dp, modifier = Modifier.align(Alignment.CenterEnd)) {}
    }
}

@Composable
private fun IconButton48(icon: ImageVector, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(size))
    }
}

// ── Header + stats ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderCard(name: String, tier: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter)
            .clip(MaterialTheme.shapes.large)
            .background(colors.surfaceContainer)
            .padding(vertical = 24.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar placeholder (green ring + person glyph) until a real profile photo is provided.
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(2.dp, colors.primary, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(NavAccount, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(
            tier.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.2.sp,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile("284", "Sessions", Modifier.weight(1f))
        StatTile("12", "PRs", Modifier.weight(1f))
        StatTile("52", "Weeks", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.primary)
        Spacer(Modifier.height(2.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.5.sp,
            color = colors.onSurfaceVariant,
        )
    }
}

// ── Settings sections ───────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = Gutter)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.0.sp,
            color = colors.primary,
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainer)
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
        ) {
            content()
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable () -> Unit = {},
) {
    val base = Modifier.fillMaxWidth()
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    Row(
        modifier = clickable.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val colors = MaterialTheme.colorScheme
    SettingRow(icon = icon, title = title, subtitle = subtitle, iconTint = iconTint) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.outline,
                uncheckedTrackColor = colors.surfaceContainerHighest,
                uncheckedBorderColor = colors.outlineVariant,
            ),
        )
    }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    SettingRow(icon = icon, title = title, subtitle = subtitle, onClick = onClick) { ChevronTrailing() }
}

@Composable
private fun ChevronTrailing() {
    Icon(
        CadenceIcons.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(7.4.dp, 12.dp),
    )
}

// ── Footer ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun Footer(onOpenCredits: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Kinetic Precision v4.8.2".uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.clip(MaterialTheme.shapes.small).clickable(onClick = onOpenCredits).padding(4.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Pro Athlete Tier Active".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.5.sp,
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

