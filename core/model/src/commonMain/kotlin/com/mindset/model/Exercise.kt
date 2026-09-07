package com.mindset.model

enum class Modality { STRENGTH, CONDITIONING, RUN, MOBILITY }

data class Exercise(
    val id: String,
    val name: String,
    val modality: Modality,
    val defaultMetric: MetricType,
    val hyroxStation: HyroxStation?,
    val category: String?,
    val force: String?,
    val level: String?,
    val mechanic: String?,
    val equipment: String?,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val imageUrls: List<String>,
)
