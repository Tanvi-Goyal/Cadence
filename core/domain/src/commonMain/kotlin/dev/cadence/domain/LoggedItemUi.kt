package dev.cadence.domain

import dev.cadence.model.BlockSection
import dev.cadence.model.ConditioningFormat
import dev.cadence.model.ExerciseEntryDetail
import dev.cadence.model.SessionDetail
import dev.cadence.model.SetEntry

/** One exercise card, resolved for display (name + metric + its sets). Shared by the logging,
 *  session-detail and template-builder ViewModels (each in its own feature module). [eachSide] and
 *  [note] are the v9 prescription hints (per-side flag + coaching cue); null/false for older data. */
data class LoggedItemUi(
    val loggedItemId: String,
    val exerciseName: String,
    val metric: String,
    val sets: List<SetEntry>,
    val eachSide: Boolean = false,
    val note: String? = null,
)

/** A titled group of exercise cards — one per [dev.cadence.model.Block] — for the block-aware Log
 *  Workout screen. [meta] carries the conditioning shape (AMRAP/EMOM/TABATA…) when present. */
data class LogSectionUi(
    val label: String,
    val meta: String?,
    val items: List<LoggedItemUi>,
)

/**
 * Flatten a hydrated [SessionDetail] into the per-exercise cards the detail/builder screens render.
 * `metric` is the domain `MetricType` name, so the existing `== ExerciseMetric.WEIGHT_REPS` check in
 * the UI still selects the strength layout. Use [toLogSections] for the block-aware Log Workout layout.
 */
fun SessionDetail?.toLoggedItemUis(): List<LoggedItemUi> =
    this?.blocks?.flatMap { it.entries }?.map { it.toLoggedItemUi() }.orEmpty()

/** Block-aware view: keep each [dev.cadence.model.Block] as its own titled section. */
fun SessionDetail?.toLogSections(): List<LogSectionUi> =
    this?.blocks?.map { block ->
        LogSectionUi(
            label = block.block.label ?: block.block.section?.let(::sectionLabel) ?: "Block",
            meta = conditioningMeta(
                block.block.conditioningFormat,
                block.block.capSeconds,
                block.block.rounds,
                block.block.workSeconds,
            ),
            items = block.entries.map { it.toLoggedItemUi() },
        )
    }.orEmpty()

private fun ExerciseEntryDetail.toLoggedItemUi(): LoggedItemUi = LoggedItemUi(
    loggedItemId = entry.id,
    exerciseName = exercise.name,
    metric = exercise.defaultMetric.name,
    sets = sets,
    eachSide = entry.eachSide,
    note = entry.note,
)

private fun sectionLabel(section: BlockSection): String = when (section) {
    BlockSection.WARMUP -> "Warm-up"
    BlockSection.MAIN -> "Main Strength"
    BlockSection.ACCESSORY -> "Accessory"
    BlockSection.CONDITIONING -> "Conditioning"
    BlockSection.CORE -> "Core"
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
