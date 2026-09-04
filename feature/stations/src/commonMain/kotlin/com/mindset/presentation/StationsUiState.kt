package com.mindset.presentation

import androidx.compose.runtime.Immutable
import com.mindset.model.HyroxStation

@Immutable
data class StationsUiState(
    val loading: Boolean = true,
    val stations: List<StationCardUi> = emptyList(),
)

@Immutable
data class StationCardUi(
    val station: HyroxStation,
    val name: String,
    val targetLabel: String,
    val recentLabel: String,
    val pbLabel: String,
    val sessionsLabel: String,
    val trend: StationTrendUi? = null,
)

@Immutable
data class StationTrendUi(val label: String, val improving: Boolean)
