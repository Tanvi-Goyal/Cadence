package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.MindSetTopBar
import com.mindset.icons.Barbell
import com.mindset.icons.Burpee
import com.mindset.icons.Dumbbell
import com.mindset.icons.Rowing
import com.mindset.icons.SkiErg
import com.mindset.icons.SledPull
import com.mindset.icons.WallBall
import com.mindset.model.BottomNavTab
import com.mindset.model.HyroxStation
import com.mindset.presentation.StationCardUi
import com.mindset.presentation.StationRecordUi
import com.mindset.presentation.StationsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Station board (records list) — every logged Hyrox station's cached PBs, grouped by station and
 * bucketed by weight class. The marquee board (recent/trend/detail) grows from this in Increment 2.
 */
@Composable
fun StationsScreen(onTab: (BottomNavTab) -> Unit, viewModel: StationsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MindSetTheme {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { MindSetTopBar(onProfileClick = { onTab(BottomNavTab.Profile) }) },
            bottomBar = { BottomNavBar(current = BottomNavTab.Stations, onTabClick = onTab) },
        ) { padding ->
            val spacing = MaterialTheme.spacing
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    start = spacing.md,
                    end = spacing.md,
                    top = padding.calculateTopPadding() + spacing.sm,
                    bottom = padding.calculateBottomPadding() + spacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                item {
                    Column {
                        SectionLabel("Competition Protocol")
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            "Station Board",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Your personal bests per station, by weight class.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (!state.loading && state.stations.isEmpty()) {
                    item {
                        EmptyHint(
                            "No station PBs yet. Log a Hyrox station to set your baseline.",
                            modifier = Modifier.fillMaxWidth().padding(top = spacing.xl),
                        )
                    }
                }

                items(state.stations, key = { it.station.name }) { card ->
                    StationCard(card)
                }
            }
        }
    }
}

@Composable
private fun StationCard(card: StationCardUi, modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.smd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.smd)) {
            IconMedallion(icon = stationIcon(card.station))
            Text(
                card.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        card.records.forEach { record -> StationRecordRow(record) }
    }
}

@Composable
private fun StationRecordRow(record: StationRecordUi, modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            record.valueLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.unitLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            record.bucketLabel?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
        record.divisionLabel?.let { AccentPill(it) }
    }
}

/** Station → icon. The three unillustrated stations (Sled Push, Farmers, Lunges) fall back for now. */
private fun stationIcon(station: HyroxStation): ImageVector = when (station) {
    HyroxStation.SKI_ERG -> MindSetIcons.SkiErg
    HyroxStation.SLED_PUSH -> MindSetIcons.SledPull
    HyroxStation.SLED_PULL -> MindSetIcons.SledPull
    HyroxStation.BURPEE_BROAD_JUMP -> MindSetIcons.Burpee
    HyroxStation.ROWING -> MindSetIcons.Rowing
    HyroxStation.FARMERS_CARRY -> MindSetIcons.Dumbbell
    HyroxStation.SANDBAG_LUNGES -> MindSetIcons.Barbell
    HyroxStation.WALL_BALLS -> MindSetIcons.WallBall
}
