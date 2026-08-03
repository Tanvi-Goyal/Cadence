package dev.cadence.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.ActiveWorkoutController
import dev.cadence.model.HyroxVariant as RaceVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/*
 * Drives the HYROX-simulation detail (Figma 33:1727 — "Full Hyrox Sim") and its "Half Sim" sibling,
 * which share one station/run block-list layout. The flow follows the official HYROX format: a 1km run
 * INTO each station, ×8 (or a slice, for the half sim).
 *
 * MVI: the screen picks a [HyroxDivision] and (half sim only) a [HyroxVariant]; the block list and its
 * division-accurate weights are derived by [HyroxStandards.buildBlocks]. Weights come from the verified
 * standards in HyroxStandards — illustrative-but-verified sample data, pending real seed data.
 */

/** Which leading glyph a Hyrox row shows. Screen maps each to a `CadenceIcons` vector. */
enum class HyroxGlyph {
    SKI_ERG, SLED_PUSH, SLED_PULL, BURPEE, ROWING, FARMERS_CARRY, SANDBAG_LUNGES, WALL_BALLS, RUN,
}

/** A row is either a functional STATION (icon medallion) or an interleaved RUN (left accent border). */
enum class HyroxRowKind { STATION, RUN }

@Immutable
data class HyroxRow(
    val kind: HyroxRowKind,
    val glyph: HyroxGlyph,
    val title: String,
    val detail: String,
    val value: String,
)

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
    private val templateId: String,
) : ViewModel() {

    private val isHalf: Boolean = templateId == "half-hyrox-sim"

    private val division = MutableStateFlow(HyroxDivision.MEN)
    private val variant = MutableStateFlow(if (isHalf) HyroxVariant.FIRST_HALF else HyroxVariant.FULL)

    /**
     * Start a live workout for the current division/variant. Hands off to the app-scoped
     * [ActiveWorkoutController] (which synthesizes + persists the session and drives the timer), so
     * the running workout survives navigation and is observable everywhere. The feature-local
     * [HyroxDivision]/[HyroxVariant] enums map by name onto the DB division key / domain [RaceVariant].
     */
    fun startWorkout() {
        controller.startHyrox(
            divisionKey = division.value.name,
            variant = RaceVariant.valueOf(variant.value.name),
            templateId = templateId,
        )
    }

    val uiState: StateFlow<TemplateHyroxDetailUiState> =
        combine(division, variant) { d, v -> buildState(d, v) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = buildState(division.value, variant.value),
            )

    fun onDivisionSelected(d: HyroxDivision) { division.value = d }

    /** Ignored for the full sim (no variant selector). */
    fun onVariantSelected(v: HyroxVariant) { if (isHalf) variant.value = v }

    private fun buildState(d: HyroxDivision, v: HyroxVariant): TemplateHyroxDetailUiState {
        val (title, duration, description) = meta(v)
        return TemplateHyroxDetailUiState(
            title = title,
            badge = "Hyrox",
            duration = duration,
            description = description,
            division = d,
            variant = v,
            showVariantSelector = isHalf,
            blocks = HyroxStandards.buildBlocks(d, v),
            finishLabel = "Finish Line",
        )
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
