package com.mindset.model

/**
 * A fully hydrated session for the Live Logging / detail screens: the session with its ordered
 * blocks, each block's ordered exercise entries, and each entry's exercise + sets. The repository
 * assembles this from the DB graph; the UI reads it without ever touching Room types.
 */
data class SessionDetail(
    val session: Session,
    val blocks: List<BlockDetail>,
)

data class BlockDetail(
    val block: Block,
    val entries: List<ExerciseEntryDetail>,
)

data class ExerciseEntryDetail(
    val entry: ExerciseEntry,
    val exercise: Exercise,
    val sets: List<SetEntry>,
    /** Which input cells this entry's rows render, derived from [Exercise.defaultMetric]. */
    val captureFields: CaptureFields = CaptureFields.of(exercise.defaultMetric),
)
