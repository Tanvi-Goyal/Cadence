package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.PreferencesRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.Gender
import com.mindset.model.HyroxStation
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxStationType
import com.mindset.model.RaceMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.mindset.model.HyroxVariant as RaceVariant

class TemplateHyroxDetailViewModel(
    private val controller: ActiveWorkoutController,
    private val repository: SessionRepository,
    private val preferences: PreferencesRepository,
    private val templateId: String,
) : ViewModel() {

    private val isHalf: Boolean = templateId == "half-hyrox-sim"

    private val division = MutableStateFlow(HyroxDivision.MEN)
    private val variant = MutableStateFlow(if (isHalf) HyroxVariant.FIRST_HALF else HyroxVariant.FULL)

    init {
        viewModelScope.launch {
            val key = preferences.getRaceInfo().first ?: return@launch
            HyroxDivision.entries.firstOrNull { it.name == key }?.let { division.value = it }
        }
    }

    fun startWorkout() {
        controller.startHyrox(
            divisionKey = division.value.name,
            variant = variant.value.toDomain(),
            templateId = templateId,
        )
    }

    val uiState: StateFlow<TemplateHyroxDetailUiState> =
        combine(division, variant) { d, v -> buildState(d, v) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyState(division.value, variant.value),
            )

    fun onDivisionSelected(d: HyroxDivision) {
        division.value = d
    }

    fun onVariantSelected(v: HyroxVariant) {
        if (isHalf) variant.value = v
    }

    private suspend fun buildState(d: HyroxDivision, v: HyroxVariant): TemplateHyroxDetailUiState {
        val raceInfo = preferences.getRaceInfo()
        val segments = repository.hyroxFormat(
            divisionKey = d.name,
            variant = v.toDomain(),
            raceMode = raceInfo.third ?: RaceMode.SINGLES,
            gender = raceInfo.second ?: if (d.name.startsWith("WOMEN")) Gender.WOMEN else Gender.MEN,
        )
        return emptyState(d, v).copy(blocks = segments.toBlocks())
    }

    private fun emptyState(d: HyroxDivision, v: HyroxVariant): TemplateHyroxDetailUiState {
        val (title, duration, description) = meta(v)
        return TemplateHyroxDetailUiState(
            title = title,
            badge = "Hyrox",
            duration = duration,
            description = description,
            division = d,
            variant = v,
            showVariantSelector = isHalf,
            blocks = emptyList(),
            finishLabel = "Finish Line",
        )
    }

    private fun List<HyroxStationModel>.toBlocks(): List<HyroxBlock> {
        val grouped = LinkedHashMap<String, MutableList<HyroxRow>>()
        val pendingRuns = mutableListOf<HyroxRow>()
        forEach { segment ->
            if (segment.stationType == HyroxStationType.RUN) {
                pendingRuns += HyroxRow(
                    kind = HyroxRowKind.RUN,
                    glyph = HyroxGlyph.RUN,
                    title = segment.title,
                    detail = segment.detail,
                    value = segment.value,
                )
                return@forEach
            }
            val rows = grouped.getOrPut(blockLabel(segment.stationNumber)) { mutableListOf() }
            rows += pendingRuns
            pendingRuns.clear()
            rows += HyroxRow(
                kind = HyroxRowKind.STATION,
                glyph = segment.station.toGlyph(),
                title = segment.title,
                detail = segment.detail,
                value = segment.value,
            )
        }
        // A trailing run (a variant ending on a run) still needs a home.
        if (pendingRuns.isNotEmpty()) {
            grouped.getOrPut(blockLabel(null)) { mutableListOf() } += pendingRuns
        }
        return grouped.map { (label, rows) -> HyroxBlock(label, rows) }
    }

    private fun blockLabel(stationNumber: Int?): String = when (stationNumber) {
        1 -> "Block 1: Start"
        2, 3 -> "Block 2: Strength"
        4 -> "Block 3: Agility"
        5 -> "Block 4: Engine"
        6, 7 -> "Block 5: Grip & Core"
        else -> "Block 6: The Finish"
    }

    private fun HyroxStation?.toGlyph(): HyroxGlyph = when (this) {
        HyroxStation.SKI_ERG -> HyroxGlyph.SKI_ERG
        HyroxStation.SLED_PUSH -> HyroxGlyph.SLED_PUSH
        HyroxStation.SLED_PULL -> HyroxGlyph.SLED_PULL
        HyroxStation.BURPEE_BROAD_JUMP -> HyroxGlyph.BURPEE
        HyroxStation.ROWING -> HyroxGlyph.ROWING
        HyroxStation.FARMERS_CARRY -> HyroxGlyph.FARMERS_CARRY
        HyroxStation.SANDBAG_LUNGES -> HyroxGlyph.SANDBAG_LUNGES
        HyroxStation.WALL_BALLS -> HyroxGlyph.WALL_BALLS
        null -> HyroxGlyph.RUN
    }

    private fun meta(v: HyroxVariant): Triple<String, String, String> = when (v) {
        HyroxVariant.FULL -> Triple(
            "Full Hyrox Simulation",
            "75-90 min",
            "A high-intensity simulation covering all 8 functional stations and interleaved running " +
                "segments. Designed for elite preparation.",
        )

        HyroxVariant.FIRST_HALF -> Triple(
            "Half Hyrox Sim",
            "35-45 min",
            "The opening four stations at full race distance — SkiErg through Burpee Broad Jumps.",
        )

        HyroxVariant.SECOND_HALF -> Triple(
            "Half Hyrox Sim",
            "35-45 min",
            "The closing four stations at full race distance — Rowing through Wall Balls.",
        )

        HyroxVariant.HALVED -> Triple(
            "Half Hyrox Sim",
            "40-50 min",
            "All eight stations at half distance and reps — full race weights, in half the time.",
        )
    }

    private fun HyroxVariant.toDomain(): RaceVariant = when (this) {
        HyroxVariant.FULL -> RaceVariant.FULL
        HyroxVariant.FIRST_HALF -> RaceVariant.FIRST_HALF
        HyroxVariant.SECOND_HALF -> RaceVariant.SECOND_HALF
        HyroxVariant.HALVED -> RaceVariant.HALVED
    }

}
