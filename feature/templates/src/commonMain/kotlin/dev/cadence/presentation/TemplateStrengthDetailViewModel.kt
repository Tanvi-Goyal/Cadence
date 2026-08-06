package dev.cadence.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.repository.SessionRepository
import dev.cadence.model.BlockDetail
import dev.cadence.model.BlockSection
import dev.cadence.model.ConditioningFormat
import dev.cadence.model.SessionDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Drives the Strength template detail — DB-backed. It observes the real seeded template
 * (`observeSessionDetail`) and maps its block tree onto the design: a hero, muscle-derived focus-area
 * chips, the session-goal card (from the template's notes), a warm-up card, the exercise sections
 * (main / accessory / conditioning) each showing the target scheme + coaching note, and an expandable
 * cool-down (the core block). Starting it deep-copies the template into a live session.
 */

/** One exercise row: name + target scheme (e.g. "8-6-4-4" or "3 × 40s"), optional cue, per-side flag. */
@Immutable
data class StrengthExerciseUi(
    val name: String,
    val scheme: String,
    val note: String?,
    val eachSide: Boolean,
)

/** One block rendered as a titled section; [meta] carries the conditioning shape when present. */
@Immutable
data class StrengthSectionUi(
    val label: String,
    val meta: String?,
    val exercises: List<StrengthExerciseUi>,
)

@Immutable
data class TemplateStrengthDetailUiState(
    val loaded: Boolean = false,
    val title: String = "",
    val focus: String = "",
    val exerciseCount: Int = 0,
    val focusAreas: List<String> = emptyList(),
    val sessionGoal: String? = null,
    val warmUp: StrengthSectionUi? = null,
    val sections: List<StrengthSectionUi> = emptyList(),
    val coolDown: StrengthSectionUi? = null,
    val coolDownExpanded: Boolean = false,
)

class TemplateStrengthDetailViewModel(
    private val repository: SessionRepository,
    private val templateId: String,
) : ViewModel() {

    private val coolDownExpanded = MutableStateFlow(false)

    val uiState: StateFlow<TemplateStrengthDetailUiState> =
        combine(repository.observeSessionDetail(templateId), coolDownExpanded) { detail, expanded ->
            detail?.let { buildState(it, expanded) } ?: TemplateStrengthDetailUiState()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TemplateStrengthDetailUiState(),
        )

    fun onToggleCoolDown() = coolDownExpanded.update { !it }

    /** Deep-copy this template into a live session, then hand its id back to open Log Workout. */
    fun start(onStarted: (String) -> Unit) {
        viewModelScope.launch { onStarted(repository.instantiateTemplate(templateId).id) }
    }

    private fun buildState(detail: SessionDetail, expanded: Boolean): TemplateStrengthDetailUiState {
        fun section(b: BlockDetail) = StrengthSectionUi(
            label = b.block.label ?: b.block.section?.let(::sectionLabel) ?: "Block",
            meta = conditioningMeta(b.block.conditioningFormat, b.block.capSeconds, b.block.rounds, b.block.workSeconds),
            exercises = b.entries.map { e ->
                StrengthExerciseUi(
                    name = e.exercise.name,
                    scheme = schemeOf(e.sets.map { it.targetReps }, e.sets.map { it.targetTimeSec }),
                    note = e.entry.note,
                    eachSide = e.entry.eachSide,
                )
            },
        )

        val warm = detail.blocks.firstOrNull { it.block.section == BlockSection.WARMUP }
        val cool = detail.blocks.firstOrNull { it.block.section == BlockSection.CORE }
        val body = detail.blocks.filter { it.block.section != BlockSection.WARMUP && it.block.section != BlockSection.CORE }

        // Focus areas = distinct primary muscles across the main + accessory work, prettified.
        val focusAreas = detail.blocks
            .filter { it.block.section == BlockSection.MAIN || it.block.section == BlockSection.ACCESSORY }
            .flatMap { it.entries }
            .flatMap { it.exercise.primaryMuscles }
            .distinct()
            .take(5)
            .map { m -> m.split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) } }

        return TemplateStrengthDetailUiState(
            loaded = true,
            title = detail.session.name,
            focus = detail.session.focus.orEmpty(),
            exerciseCount = detail.blocks.sumOf { it.entries.size },
            focusAreas = focusAreas,
            sessionGoal = detail.session.notes?.takeIf { it.isNotBlank() },
            warmUp = warm?.let(::section),
            sections = body.map(::section),
            coolDown = cool?.let(::section),
            coolDownExpanded = expanded,
        )
    }

    private fun sectionLabel(section: BlockSection): String = when (section) {
        BlockSection.WARMUP -> "Warm-up"
        BlockSection.MAIN -> "Main Strength"
        BlockSection.ACCESSORY -> "Accessory"
        BlockSection.CONDITIONING -> "Conditioning"
        BlockSection.CORE -> "Core"
    }

    /** Target scheme string — reps as "8-6-4", or timed holds as "3 × 40s". */
    private fun schemeOf(reps: List<Int?>, times: List<Int?>): String {
        val repList = reps.filterNotNull()
        if (repList.isNotEmpty()) return repList.joinToString("-")
        val timeList = times.filterNotNull()
        if (timeList.isNotEmpty()) {
            val secs = timeList.first()
            return if (timeList.size > 1) "${timeList.size} × ${secs}s" else "${secs}s"
        }
        return ""
    }

    private fun conditioningMeta(format: ConditioningFormat?, capSeconds: Long?, rounds: Int?, workSeconds: Long?): String? {
        if (format == null) return null
        val cap = capSeconds?.let { " • ${it / 60} min" } ?: ""
        return when (format) {
            ConditioningFormat.AMRAP -> "AMRAP$cap"
            ConditioningFormat.EMOM -> "EMOM$cap"
            ConditioningFormat.TABATA -> "TABATA • ${workSeconds ?: 20}s / 10s × ${rounds ?: 8}"
            ConditioningFormat.FOR_TIME -> "For time$cap"
        }
    }
}
