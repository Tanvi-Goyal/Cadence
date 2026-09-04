package com.mindset.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.ActiveWorkoutController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.mindset.domain.repository.PreferencesRepository
import kotlinx.coroutines.launch
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.Gender
import com.mindset.model.HyroxStation
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxStationType
import com.mindset.model.RaceMode
import com.mindset.model.HyroxVariant as RaceVariant

/*
 * Drives the HYROX-simulation detail (Figma 33:1727 — "Full Hyrox Sim") and its "Half Sim" sibling,
 * which share one station/run block-list layout. The flow follows the official HYROX format: a 1km run
 * INTO each station, ×8 (or a slice, for the half sim).
 *
 * MVI: the screen picks a [HyroxDivision] and (half sim only) a [HyroxVariant]; the block list and its
 * division-accurate weights come from the SEEDED reference format (`SessionRepository.hyroxFormat`) —
 * the same source the live timer runs from, so the weights previewed here are the ones actually raced.
 * The division defaults to the athlete's profile and the picker overrides it for the preview.
 * `HyroxStandards` now supplies only the @Preview's sample blocks, not runtime standards.
 */

/** Which leading glyph a Hyrox row shows. Screen maps each to a `MindSetIcons` vector. */
enum class HyroxGlyph {
    SKI_ERG,
    SLED_PUSH,
    SLED_PULL,
    BURPEE,
    ROWING,
    FARMERS_CARRY,
    SANDBAG_LUNGES,
    WALL_BALLS,
    RUN,
}

/** A row is either a functional STATION (icon medallion) or an interleaved RUN (left accent border). */
enum class HyroxRowKind { STATION, RUN }

@Immutable
data class HyroxRow(val kind: HyroxRowKind, val glyph: HyroxGlyph, val title: String, val detail: String, val value: String)

/** A titled block of rows (e.g. "BLOCK 1: START"). */
@Immutable
data class HyroxBlock(val label: String, val rows: List<HyroxRow>)

@Immutable
data class TemplateHyroxDetailUiState(
    val title: String,
    val badge: String,
    val duration: String,
    val description: String,
    val division: HyroxDivision,
    val variant: HyroxVariant,
    /** Whether the 1st/2nd/halved variant chips are shown (half sim only). */
    val showVariantSelector: Boolean,
    val blocks: List<HyroxBlock>,
    /** Label for the finish-line indicator that closes the flow after the final station. */
    val finishLabel: String,
)

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
        // Open on the athlete's own division rather than the MEN default, so the previewed weights —
        // and the race started from them — match their race weight without touching the picker.
        // The enum names are the seeded `event_division` keys (WOMEN / MEN / WOMEN_PRO / MEN_PRO).
        viewModelScope.launch {
            val key = preferences.getRaceInfo().first ?: return@launch
            HyroxDivision.entries.firstOrNull { it.name == key }?.let { division.value = it }
        }
    }

    /**
     * Start a live workout for the current division/variant. Hands off to the app-scoped
     * [ActiveWorkoutController] (which synthesizes + persists the session and drives the timer), so
     * the running workout survives navigation and is observable everywhere. The feature-local
     * [HyroxDivision]/[HyroxVariant] enums map by name onto the DB division key / domain [RaceVariant].
     */
    fun startWorkout() {
        controller.startHyrox(
            divisionKey = division.value.name,
            variant = variant.value.toDomain(),
            templateId = templateId,
        )
    }

    /**
     * Feature-local [HyroxVariant] → domain [RaceVariant]. Mapped exhaustively rather than by
     * `valueOf(name)` so renaming either enum is a compile error instead of a runtime crash.
     */
    private fun HyroxVariant.toDomain(): RaceVariant = when (this) {
        HyroxVariant.FULL -> RaceVariant.FULL
        HyroxVariant.FIRST_HALF -> RaceVariant.FIRST_HALF
        HyroxVariant.SECOND_HALF -> RaceVariant.SECOND_HALF
        HyroxVariant.HALVED -> RaceVariant.HALVED
    }

    val uiState: StateFlow<TemplateHyroxDetailUiState> =
        combine(division, variant) { d, v -> buildState(d, v) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                // The block list is now a suspend DB read, so the first frame renders the chrome with
                // no rows rather than fabricating standards synchronously.
                initialValue = emptyState(division.value, variant.value),
            )

    fun onDivisionSelected(d: HyroxDivision) {
        division.value = d
    }

    /** Ignored for the full sim (no variant selector). */
    fun onVariantSelected(v: HyroxVariant) {
        if (isHalf) variant.value = v
    }

    /**
     * Builds the block list from the **seeded** reference format, so the weights previewed here are
     * exactly the ones a race started from this screen will run (both now read [SessionRepository.hyroxFormat]).
     * Replaces the in-code `HyroxStandards.buildBlocks` numbers; only the thematic block grouping below
     * remains in code, because it is presentation and the seed carries no equivalent.
     */
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

    /**
     * Groups the flat race order into the design's thematic blocks. A run belongs to the block of the
     * station it leads INTO, which is why runs are buffered until their station arrives.
     */
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

    /** Presentation-only: which thematic block a station's race number falls in. */
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

    // Title/duration/description per variant. Durations are illustrative (not authoritative).
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
}
