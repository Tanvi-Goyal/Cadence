package com.mindset.model

/**
 * A catalog exercise. **Not** [Syncable] — the library is identical seeded reference data on every
 * device (referenced by [id]), so it never travels the sync wire. Unlike the Room entity (which
 * stores list columns as JSON text), the domain type exposes real [List]s; the mapper decodes.
 *
 * v8 additions: [modality] and [defaultMetric] (first-class, replacing the entity's 2-value
 * `metric`) and [hyroxStation] — non-null tags this as one of the 8 Hyrox stations.
 */
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
