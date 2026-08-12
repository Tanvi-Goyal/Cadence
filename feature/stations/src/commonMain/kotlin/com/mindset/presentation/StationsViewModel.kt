package com.mindset.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.HyroxStation
import com.mindset.model.PrKind
import com.mindset.model.StationRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Render-ready state for the Station board's records list. Grouped by station, formatted to strings. */
@Immutable
data class StationsUiState(
    val loading: Boolean = true,
    val stations: List<StationCardUi> = emptyList(),
)

@Immutable
data class StationCardUi(
    val station: HyroxStation,
    val name: String,
    val records: List<StationRecordUi>,
)

@Immutable
data class StationRecordUi(
    val valueLabel: String,
    val unitLabel: String,
    val bucketLabel: String?,
    val divisionLabel: String?,
)

/**
 * Backs the Station board: all cached Hyrox PBs (division-bucketed) grouped by station and formatted
 * to render-ready strings, so the Composable never touches domain types. Reads the `personal_records`
 * cache — the single source of PB truth — via [SessionRepository.observeStationRecords].
 */
class StationsViewModel(private val repository: SessionRepository) : ViewModel() {
    val uiState: StateFlow<StationsUiState> =
        repository.observeStationRecords()
            .map { records -> StationsUiState(loading = false, stations = groupByStation(records)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = StationsUiState(),
            )
}

private fun groupByStation(records: List<StationRecord>): List<StationCardUi> =
    records.groupBy { it.station }
        .map { (station, recs) ->
            StationCardUi(
                station = station,
                name = recs.firstOrNull()?.displayName ?: station.name,
                records = recs.map { it.toUi() },
            )
        }
        .sortedBy { it.station.ordinal }

private fun StationRecord.toUi(): StationRecordUi = StationRecordUi(
    valueLabel = when (kind) {
        PrKind.BEST_TIME -> formatTime(value.toInt())
        PrKind.MAX_REPS, PrKind.MAX_CALORIES -> value.toInt().toString()
        PrKind.MAX_WEIGHT, PrKind.EST_1RM -> "${trimKg(value)} kg"
    },
    unitLabel = when (kind) {
        PrKind.BEST_TIME -> "best time"
        PrKind.MAX_REPS -> "max reps"
        PrKind.MAX_CALORIES -> "max cal"
        PrKind.MAX_WEIGHT -> "max load"
        PrKind.EST_1RM -> "est. 1RM"
    },
    bucketLabel = bucket?.let { b -> if (station == HyroxStation.WALL_BALLS) "$b reps" else "${b}m" },
    divisionLabel = divisionKey?.let(::formatDivision),
)

/** Whole seconds → `M:SS` (e.g. 135 → "2:15"). */
private fun formatTime(totalSec: Int): String = "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"

/** Drop a trailing `.0` so 152.0 → "152", keep one decimal otherwise. */
private fun trimKg(kg: Double): String = if (kg % 1.0 == 0.0) kg.toInt().toString() else ((kg * 10).toInt() / 10.0).toString()

/** `MEN_PRO` → "Men Pro". Cosmetic; `event_division` carries canonical labels for a later refinement. */
private fun formatDivision(key: String): String =
    key.split("_").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
