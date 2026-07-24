package dev.cadence.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*
 * Drives the Hyrox-simulation variant of Template Detail (Figma 33:1727 — "Full Hyrox Sim"). Same
 * design-accurate-static approach as the other detail screens: one immutable state seeded with the
 * mock's content, exposed via StateFlow. It is a distinct layout from the interval-run detail (a
 * station/run block list rather than a work-protocol), so it has its own screen + route for now;
 * unifying detail layouts behind a data-driven "template kind" is a later pass.
 */

/** Leading glyph for a Hyrox row. Semantic enum → screen maps to a `CadenceIcons` vector. Covers all
 *  eight official HYROX stations (SkiErg, Sled Push, Sled Pull, Burpee Broad Jump, Rowing, Kettlebell
 *  Farmers Carry, Sandbag Lunges, Wall Balls) plus the 1km run between them. */
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
    val blocks: List<HyroxBlock>,
    /** Label for the finish-line indicator that closes the flow after the final station. */
    val finishLabel: String,
)

class TemplateHyroxDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SEED)
    val uiState: StateFlow<TemplateHyroxDetailUiState> = _uiState.asStateFlow()

    private companion object {
        private fun station(glyph: HyroxGlyph, title: String, detail: String, value: String) =
            HyroxRow(HyroxRowKind.STATION, glyph, title, detail, value)

        private fun run(title: String, detail: String, value: String) =
            HyroxRow(HyroxRowKind.RUN, HyroxGlyph.RUN, title, detail, value)

        // Design sample content (Figma 33:1727), extended to the full 8-station HYROX flow. The
        // official order is: 1km run → station, ×8 (SkiErg, Sled Push, Sled Pull, Burpee Broad Jump,
        // Rowing, Kettlebell Farmers Carry, Sandbag Lunges, Wall Balls). Station weights/reps are
        // division-dependent and shown here as ILLUSTRATIVE sample values — they are not authoritative
        // and must come from verified seed data before shipping (see the Hyrox-standards note).
        val SEED = TemplateHyroxDetailUiState(
            title = "Full Hyrox Simulation",
            badge = "Hyrox",
            duration = "75-90 min",
            description = "A high-intensity simulation covering all 8 functional stations and " +
                "interleaved running segments. Designed for elite preparation.",
            blocks = listOf(
                HyroxBlock(
                    label = "Block 1: Start",
                    rows = listOf(
                        station(HyroxGlyph.SKI_ERG, "1. SkiErg", "1000m Distance", "1000m"),
                        run("Run 1", "Aerobic Base", "1.0 km"),
                    ),
                ),
                HyroxBlock(
                    label = "Block 2: Strength",
                    rows = listOf(
                        station(HyroxGlyph.SLED_PUSH, "2. Sled Push", "50m Heavy Carry", "175 kg"),
                        run("Run 2", "Pacing Strategy", "1.0 km"),
                        station(HyroxGlyph.SLED_PULL, "3. Sled Pull", "50m Resistance", "125 kg"),
                        run("Run 3", "Interval Mode", "1.0 km"),
                    ),
                ),
                HyroxBlock(
                    label = "Block 3: Agility",
                    rows = listOf(
                        station(HyroxGlyph.BURPEE, "4. Burpee Broad Jumps", "80m Ground Coverage", "80m"),
                        run("Run 4", "Recovery Pace", "1.0 km"),
                    ),
                ),
                HyroxBlock(
                    label = "Block 4: Engine",
                    rows = listOf(
                        station(HyroxGlyph.ROWING, "5. Rowing", "1000m Distance", "1000m"),
                        run("Run 5", "Steady State", "1.0 km"),
                    ),
                ),
                HyroxBlock(
                    label = "Block 5: Grip & Core",
                    rows = listOf(
                        station(HyroxGlyph.FARMERS_CARRY, "6. Farmers Carry", "200m Kettlebell Carry", "2×32 kg"),
                        run("Run 6", "Threshold Pace", "1.0 km"),
                        station(HyroxGlyph.SANDBAG_LUNGES, "7. Sandbag Lunges", "100m Walking Lunges", "30 kg"),
                        run("Run 7", "Final Push", "1.0 km"),
                    ),
                ),
                HyroxBlock(
                    label = "Block 6: The Finish",
                    rows = listOf(
                        station(HyroxGlyph.WALL_BALLS, "8. Wall Balls", "9kg Ball • 3m Target", "100 reps"),
                    ),
                ),
            ),
            finishLabel = "Finish Line",
        )
    }
}
