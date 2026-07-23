package dev.cadence.domain

import dev.cadence.model.MuscleDiagram

/**
 * Resolves an exercise's muscle name to a [MuscleDiagram] (base body + highlighted overlay). The
 * implementation (wger-backed) lives in `:core:data`; features depend only on this interface.
 */
interface MuscleImageProvider {
    suspend fun diagram(muscleName: String): MuscleDiagram?
}
