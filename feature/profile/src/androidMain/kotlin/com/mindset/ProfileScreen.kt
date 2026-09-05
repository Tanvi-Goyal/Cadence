package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindset.components.BottomNavBar
import com.mindset.components.MindSetTopBar
import com.mindset.model.BottomNavTab
import com.mindset.presentation.ProfileUiState
import com.mindset.presentation.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

/*
 * Profile. The athlete's identity and training shape: who they are, their headline numbers, how
 * often they have trained recently, and which integrations are coming.
 *
 * Everything rendered here is real, observed data — there is no placeholder copy standing in for a
 * value the app could know. The one deliberate exception is the integrations list, which is a
 * roadmap rather than a data source and says so on every row.
 *
 * Hyrox station PBs are absent on purpose: the Stations tab owns them, and duplicating them here
 * would mean two places to keep correct.
 */

@Composable
fun ProfileScreen(onTab: (BottomNavTab) -> Unit, viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MindSetTheme {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { MindSetTopBar(onProfileClick = { onTab(BottomNavTab.Profile) }) },
            bottomBar = { BottomNavBar(current = BottomNavTab.Profile, onTabClick = onTab) },
        ) { inner ->
            ProfileContent(state = state, contentPadding = inner)
        }
    }
}

@Composable
private fun ProfileContent(state: ProfileUiState, contentPadding: PaddingValues) {
    val spacing = MaterialTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = spacing.md,
            end = spacing.md,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        // Held back until the first DB emission so the header never flashes "Athlete" and a 0 streak.
        if (state.isLoading) return@LazyColumn

        item(key = "header") {
            ProfileHeaderCard(name = state.athleteName, tierLabel = state.tierLabel)
        }

        item(key = "stats") {
            ProfileStatsRow(streakDays = state.streakDays, totalSessions = state.totalSessions)
        }

        if (state.frequency.isNotEmpty()) {
            item(key = "frequency") { TrainingFrequencyCard(weeks = state.frequency) }
        }

        item(key = "integrations") { IntegrationsCard() }
    }
}
