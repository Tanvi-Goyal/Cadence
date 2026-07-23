package dev.cadence.domain

import dev.cadence.model.SessionDetail
import dev.cadence.model.SetEntry

/** One exercise card, resolved for display (name + metric + its sets). Shared by the logging,
 *  session-detail and template-builder ViewModels (each in its own feature module). */
data class LoggedItemUi(
    val loggedItemId: String,
    val exerciseName: String,
    val metric: String,
    val sets: List<SetEntry>,
)

/**
 * Flatten a hydrated [SessionDetail] into the per-exercise cards the logging/detail/builder screens
 * render. `metric` is the domain `MetricType` name, so the existing `== ExerciseMetric.WEIGHT_REPS`
 * check in the UI still selects the strength layout. (Blocks are flattened for now; the block-aware
 * UI arrives with the real block model.)
 */
fun SessionDetail?.toLoggedItemUis(): List<LoggedItemUi> =
    this?.blocks?.flatMap { it.entries }?.map { entry ->
        LoggedItemUi(
            loggedItemId = entry.entry.id,
            exerciseName = entry.exercise.name,
            metric = entry.exercise.defaultMetric.name,
            sets = entry.sets,
        )
    }.orEmpty()
