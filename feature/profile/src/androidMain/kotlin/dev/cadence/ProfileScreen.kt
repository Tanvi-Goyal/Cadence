package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.domain.ThemeMode
import dev.cadence.domain.WeightUnit
import dev.cadence.presentation.PreferencesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onTab: (Tab) -> Unit,
    onOpenCredits: () -> Unit,
    viewModel: PreferencesViewModel = koinViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    CadenceTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = { CadenceBottomBar(current = Tab.Profile, onTab = onTab) },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    Text("Profile", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    PreferenceSection("Weight units") {
                        WeightUnit.entries.forEach { unit ->
                            Pill(
                                label = when (unit) {
                                    WeightUnit.KG -> "Kilograms (kg)"
                                    WeightUnit.LB -> "Pounds (lb)"
                                },
                                selected = prefs.weightUnit == unit,
                                onClick = { viewModel.onWeightUnitChange(unit) },
                            )
                        }
                    }
                }
                item {
                    PreferenceSection("Theme") {
                        ThemeMode.entries.forEach { mode ->
                            Pill(
                                label = when (mode) {
                                    ThemeMode.SYSTEM -> "System"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                },
                                selected = prefs.themeMode == mode,
                                onClick = { viewModel.onThemeModeChange(mode) },
                            )
                        }
                    }
                }
                item {
                    PreferenceSection("About") {
                        Pill(label = "Open data & credits", selected = false, onClick = onOpenCredits)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
private fun Pill(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Accent else Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            label,
            color = if (selected) OnAccent else TextPrimary,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
