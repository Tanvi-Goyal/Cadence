package dev.cadence.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/*
 * Drives the Strength template detail (Push / Pull / Lower Body), the strength counterpart to the
 * HYROX sim detail. Unlike the interval "workout protocol" screen, a strength session is a list of
 * exercises grouped into supersets (A1/A2 …) over a TARGET / PREV / RPE / TEMPO stat table, framed by
 * a warm-up and an expandable cool-down.
 *
 * MVI: the id selects one of three [StrengthSession]s; the (design-static, illustrative) content is
 * built by [StrengthSessions.buildSession]. The only interactive state is whether the cool-down card
 * is expanded — held in the VM (not UI `remember`) so it survives config changes and stays the single
 * source of truth, matching the convention in [TemplateLibraryViewModel].
 */

/** Which strength session this detail renders. Maps 1:1 to the library's three Strength block ids. */
enum class StrengthSession { PUSH, PULL, LOWER }

/** Semantic leading glyph for an exercise row; the screen maps each to a `CadenceIcons` vector so the
 *  state carries no Compose UI types. */
enum class StrengthGlyph { BARBELL, DUMBBELL, MACHINE, LOWER_BODY, BODYWEIGHT }

/** One cell of an exercise's stat table. [muted] renders the value dimmed — used for PREV, which has
 *  no real history in a template. */
@Immutable
data class StrengthStat(val label: String, val value: String, val muted: Boolean = false)

@Immutable
data class StrengthExercise(
    /** Superset position tag, e.g. "A1", "A2", "B1". */
    val tag: String,
    val glyph: StrengthGlyph,
    val name: String,
    /** One-line scheme, e.g. "4 Sets • 8-10 Reps • 120s Rest". */
    val scheme: String,
    /** TARGET / PREV / RPE / TEMPO cells, in display order. */
    val stats: List<StrengthStat>,
)

/** A group of exercises performed together. More than one member renders as a bracketed superset;
 *  a single member renders as a standalone card. */
@Immutable
data class ExerciseGroup(val exercises: List<StrengthExercise>)

/** A warm-up or cool-down row (icon + title + detail + duration). */
@Immutable
data class ProtocolItem(
    val glyph: BlockGlyph,
    val title: String,
    val detail: String,
    val duration: String,
)

@Immutable
data class TemplateStrengthDetailUiState(
    val session: StrengthSession,
    val title: String,
    val subtitle: String,
    /** Hero pills: a neutral STRENGTH tag + the focus tag (`danger = true` gives it the accent tone). */
    val tags: List<DetailTag>,
    val duration: String,
    val calories: String,
    val focusAreas: List<String>,
    val sessionGoal: String,
    val warmUp: ProtocolItem,
    val groups: List<ExerciseGroup>,
    val coolDownTitle: String,
    val coolDownDuration: String,
    val coolDownItems: List<ProtocolItem>,
    val coolDownExpanded: Boolean,
)

class TemplateStrengthDetailViewModel(templateId: String) : ViewModel() {

    private val session: StrengthSession = when (templateId) {
        "upper-strength-b" -> StrengthSession.PULL
        "lower-body" -> StrengthSession.LOWER
        else -> StrengthSession.PUSH // "upper-strength-a"
    }

    /** The one piece of interactive state; content is otherwise fully derived from [session]. */
    private val coolDownExpanded = MutableStateFlow(false)

    private val content: TemplateStrengthDetailUiState = StrengthSessions.buildSession(session)

    val uiState: StateFlow<TemplateStrengthDetailUiState> =
        coolDownExpanded
            .map { expanded -> content.copy(coolDownExpanded = expanded) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = content,
            )

    fun onToggleCoolDown() { coolDownExpanded.update { !it } }
}
