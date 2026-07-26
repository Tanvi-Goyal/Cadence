package dev.cadence.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*
 * Drives the Template Detail screen (Figma 33:1455 — "Interval Run"). Same design-accurate-static
 * approach as [TemplateLibraryViewModel]: the state is seeded with the mock's content and exposed as
 * one immutable [TemplateDetailUiState] via StateFlow. Per-template content (keyed off the tapped
 * template id) and the real "instantiate → log" start flow are a later pass that touches :shared, so
 * this VM ignores the id and always serves the Interval Run protocol for now.
 */

/** A hero pill tag; [danger] flips it from the lime accent tone to the error tone (e.g. HIGH INTENSITY). */
@Immutable
data class DetailTag(val text: String, val danger: Boolean = false)

/** Leading glyph for a simple protocol block. Semantic enum → screen maps to a `CadenceIcons` vector. */
enum class BlockGlyph { WARM_UP, COOL_DOWN }

/** A plain protocol block (warm-up / cool-down): medallion glyph + title + duration + detail line. */
@Immutable
data class ProtocolBlock(
    val glyph: BlockGlyph,
    val title: String,
    val duration: String,
    val detail: String,
)

/** One labelled metric inside the work segment (e.g. GOAL PACE → 3:45/km). */
@Immutable
data class WorkStat(val label: String, val value: String)

/** The highlighted interval "work" block: a work sub-block (with stats) over a recovery sub-block. */
@Immutable
data class WorkSegment(
    val label: String,
    val repeats: String,
    val exercise: String,
    val stats: List<WorkStat>,
    val recoveryTitle: String,
    val recoveryDuration: String,
    val recoveryDetail: String,
)

@Immutable
data class TemplateDetailUiState(
    val title: String,
    val tags: List<DetailTag>,
    val duration: String,
    val calories: String,
    val protocolCount: String,
    val warmUp: ProtocolBlock,
    val work: WorkSegment,
    val coolDown: ProtocolBlock,
    val coachNote: String,
    val coachName: String,
)

class TemplateDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SEED)
    val uiState: StateFlow<TemplateDetailUiState> = _uiState.asStateFlow()

    private companion object {
        // Design sample content (Figma 33:1455). Replaced by real per-template data in a later pass.
        val SEED = TemplateDetailUiState(
            title = "VO2 Max Intervals",
            tags = listOf(DetailTag("Run"), DetailTag("High Intensity", danger = true)),
            duration = "40 MIN",
            calories = "~450 KCAL",
            protocolCount = "4 BLOCKS",
            warmUp = ProtocolBlock(
                glyph = BlockGlyph.WARM_UP,
                title = "Warm-up",
                duration = "10:00",
                detail = "Easy Run (Z1-Z2 effort)",
            ),
            work = WorkSegment(
                label = "Work Segment",
                repeats = "5x Repeats",
                exercise = "400m Sprints",
                stats = listOf(
                    WorkStat("Goal Pace", "3:45/km"),
                    WorkStat("Intensity", "VO2 MAX"),
                ),
                recoveryTitle = "Active Recovery",
                recoveryDuration = "1:30",
                recoveryDetail = "90s Walk or Light Jog",
            ),
            coolDown = ProtocolBlock(
                glyph = BlockGlyph.COOL_DOWN,
                title = "Cool-down",
                duration = "5:00",
                detail = "Walk / Static Stretching",
            ),
            coachNote = "\"Focus on vertical oscillation and driving your knees. The goal is to " +
                "maximize oxygen uptake; the final 100m of each set should feel like you're at your " +
                "limit. Keep the rest intervals strictly 'active' to prevent stiffness.\"",
            coachName = "Coach Marcus",
        )
    }
}
