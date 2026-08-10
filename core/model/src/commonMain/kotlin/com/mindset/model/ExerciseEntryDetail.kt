package com.mindset.model

data class ExerciseEntryDetail(
    val entry: ExerciseEntry,
    val exercise: Exercise,
    val sets: List<SetEntry>,
    val captureFields: CaptureFields = CaptureFields.of(exercise.defaultMetric),
)
