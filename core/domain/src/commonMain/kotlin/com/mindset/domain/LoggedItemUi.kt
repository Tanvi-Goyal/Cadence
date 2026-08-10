package com.mindset.domain

import com.mindset.model.BlockSection
import com.mindset.model.ExerciseEntryDetail
import com.mindset.model.SessionDetail
import com.mindset.model.SetEntry

data class LoggedItemUi(
    val loggedItemId: String,
    val exerciseName: String,
    val metric: String,
    val sets: List<SetEntry>,
    val eachSide: Boolean = false,
    val note: String? = null,
    val segmentKey: String? = null,
)

/**
 * Flatten a hydrated [SessionDetail] into the per-exercise cards the detail/builder screens render.
 * `metric` is the domain `MetricType` name, so the existing `== ExerciseMetric.WEIGHT_REPS` check in
 * the UI still selects the strength layout. Use [toLogSections] for the block-aware Log Workout layout.
 */
fun SessionDetail?.toLoggedItemUis(): List<LoggedItemUi> = this
    ?.blocks
    ?.flatMap { it.entries }
    ?.map { it.toLoggedItemUi() }
    .orEmpty()

/** Block-aware view: keep each [com.mindset.model.Block] as its own titled section. */
fun SessionDetail?.toLogSections(): List<LogSectionUi> = this
    ?.blocks
    ?.map { block ->
        LogSectionUi(
//        label = block.block.label ?: block.block.section?.let(::sectionLabel) ?: "Block",
            label = "Block",
            meta = "",
            items = block.entries.map { it.toLoggedItemUi() },
        )
    }.orEmpty()

private fun ExerciseEntryDetail.toLoggedItemUi(): LoggedItemUi = LoggedItemUi(
    loggedItemId = entry.id,
    exerciseName = exercise.name,
    metric = exercise.defaultMetric.name,
    sets = sets,
    eachSide = entry.eachSide,
    segmentKey = entry.segmentKey,
)

private fun sectionLabel(section: BlockSection): String = when (section) {
    BlockSection.WARMUP -> "Warm-up"
    BlockSection.MAIN -> "Main Strength"
    BlockSection.ACCESSORY -> "Accessory"
    BlockSection.CONDITIONING -> "Conditioning"
    BlockSection.CORE -> "Core"
}
