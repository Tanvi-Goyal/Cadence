package dev.cadence.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/*
 * Drives the Template Library screen (Figma 33:1068). This first pass is a design-accurate surface:
 * the state is seeded with the mock's sample content so the layout is exact and reviewable, while the
 * screen observes a single immutable [TemplateLibraryUiState] via StateFlow — the same MVI seam every
 * other screen uses. Wiring the curated/self-programmed content to the real DB (templates are Session
 * rows with isTemplate=true; there is no category/curation column yet) is a later change that touches
 * :shared, so it is deliberately out of scope here. Selection lives in the VM (an intent → new state),
 * not in UI-local `remember`, so state survives config changes and stays the single source of truth.
 */

/** Which leading glyph a card shows. Kept as a semantic enum so [TemplateLibraryUiState] carries no
 *  Compose UI types — the screen maps each value to a `CadenceIcons` vector + tint. */
enum class TemplateGlyph { STOPWATCH, ENGINE, DUMBBELL, BARBELL, LOWER_BODY, LONG_RUN, TIMER, STRETCH }

/** The small uppercase type tag on a card (SIM / ELITE / STRENGTH / RUN / FLOW). */
enum class TemplateBadge { SIM, ELITE, STRENGTH, RUN, FLOW }

/** The category buckets the library groups templates into; also the horizontal filter chips. */
enum class TemplateCategory(val label: String) {
    ALL("All Templates"),
    CLASS("Class Templates"),
    STRENGTH("Strength"),
    ENDURANCE("Endurance"),
}

/** A standard template card (bordered detail card or endurance icon-row card). */
@Immutable
data class LibraryTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: TemplateBadge,
    val glyph: TemplateGlyph,
    /** Optional secondary pill under the body (class-sim cards only); null for block/endurance cards. */
    val pill: String? = null,
)

/** The photo-backed hero card at the top of Class Templates (the official full simulation). */
@Immutable
data class FeaturedTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val flag: String,
    val tags: List<String>,
)

/** The photo-backed "most recent" card in Self-Programmed. */
@Immutable
data class RecentTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
)

/** The compact grid card in Self-Programmed. */
@Immutable
data class GridTemplate(
    val id: String,
    val title: String,
    val meta: String,
)

@Immutable
data class TemplateLibraryUiState(
    val query: String = "",
    val selectedCategory: TemplateCategory = TemplateCategory.ALL,
    val featured: FeaturedTemplate? = null,
    val classTemplates: List<LibraryTemplate> = emptyList(),
    val strengthBlocks: List<LibraryTemplate> = emptyList(),
    val endurance: List<LibraryTemplate> = emptyList(),
    val recent: RecentTemplate? = null,
    val grid: GridTemplate? = null,
) {
    /** Whether a section is visible under the current chip filter. `ALL` shows everything; a specific
     *  chip narrows to that one bucket. Self-Programmed is only shown under `ALL`. */
    val showClass: Boolean get() = selectedCategory == TemplateCategory.ALL || selectedCategory == TemplateCategory.CLASS
    val showStrength: Boolean get() = selectedCategory == TemplateCategory.ALL || selectedCategory == TemplateCategory.STRENGTH
    val showEndurance: Boolean get() = selectedCategory == TemplateCategory.ALL || selectedCategory == TemplateCategory.ENDURANCE
    val showSelfProgrammed: Boolean get() = selectedCategory == TemplateCategory.ALL
}

class TemplateLibraryViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow(TemplateCategory.ALL)

    /** The Strength Blocks section is now real: the seeded HyFit program days observed from the DB
     *  (ordered by week). The other sections remain design-static until their own data pass. */
    val uiState: StateFlow<TemplateLibraryUiState> =
        combine(query, category, repository.observeTemplates()) { q, cat, templates ->
            val strength = templates
                .filter { it.category == HYFIT_CATEGORY }
                .sortedWith(compareBy({ it.programWeek ?: 0 }, { it.name }))
                .map {
                    LibraryTemplate(
                        id = it.id,
                        title = it.name,
                        subtitle = listOfNotNull(it.programWeek?.let { w -> "Week $w" }, it.focus.takeIf { f -> !f.isNullOrBlank() })
                            .joinToString(" • "),
                        badge = TemplateBadge.STRENGTH,
                        glyph = TemplateGlyph.DUMBBELL,
                    )
                }
            SEED.copy(
                query = q,
                selectedCategory = cat,
                strengthBlocks = strength.ifEmpty { SEED.strengthBlocks },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SEED)

    fun onCategorySelected(category: TemplateCategory) {
        this.category.update { category }
    }

    fun onQueryChange(query: String) {
        this.query.update { query }
    }

    private companion object {
        /** Category tag the seeder ([TemplateSeed]) stamps on the HyFit program days. */
        const val HYFIT_CATEGORY = "HyFit 6-Week Strength"

        // Design sample content (Figma 33:1068). Strength blocks are overridden with DB data above;
        // the rest remains sample content until their own data pass.
        val SEED = TemplateLibraryUiState(
            featured = FeaturedTemplate(
                id = "full-hyrox-simulation",
                title = "Full Hyrox Simulation",
                subtitle = "8x1km Run • All 8 Functional Stations",
                flag = "Official Sim",
                tags = listOf("Conditioning", "Run"),
            ),
            classTemplates = listOf(
                LibraryTemplate("half-hyrox-sim", "Half Hyrox Sim", "4x1km • 4 Stations • Comp Pace", TemplateBadge.SIM, TemplateGlyph.STOPWATCH, pill = "Conditioning"),
                LibraryTemplate("engine-room-60", "Engine Room 60", "12 Exercises • 60 mins • High Intensity", TemplateBadge.ELITE, TemplateGlyph.ENGINE, pill = "Run"),
            ),
            strengthBlocks = listOf(
                LibraryTemplate("upper-strength-a", "Upper Strength A (Push)", "Chest, Shoulders, Triceps • 45 min", TemplateBadge.STRENGTH, TemplateGlyph.DUMBBELL),
                LibraryTemplate("upper-strength-b", "Upper Strength B (Pull)", "Back, Biceps, Rear Delts • 45 min", TemplateBadge.STRENGTH, TemplateGlyph.BARBELL),
                LibraryTemplate("lower-body", "Lower Body (Squat/Hinge)", "Quads, Glutes, Hamstrings • 50 min", TemplateBadge.STRENGTH, TemplateGlyph.LOWER_BODY),
            ),
            endurance = listOf(
                LibraryTemplate("long-run-zone-2", "Long Run (Zone 2)", "Aerobic Base • 60-90 mins", TemplateBadge.RUN, TemplateGlyph.LONG_RUN),
                LibraryTemplate("vo2-max-intervals", "VO2 Max Intervals", "400m Sprints • High Intensity", TemplateBadge.RUN, TemplateGlyph.TIMER),
                LibraryTemplate("recovery-flow", "Recovery Flow", "Dynamic Stretch • 20 mins", TemplateBadge.FLOW, TemplateGlyph.STRETCH),
            ),
            recent = RecentTemplate(
                id = "heavy-squat-focus-a",
                title = "Heavy Squat Focus A",
                subtitle = "Modified 2 days ago • 5 Exercises",
            ),
            grid = GridTemplate(
                id = "mobility-flow-2",
                title = "Mobility Flow #2",
                meta = "15 MIN • RECOVERY",
            ),
        )
    }
}
