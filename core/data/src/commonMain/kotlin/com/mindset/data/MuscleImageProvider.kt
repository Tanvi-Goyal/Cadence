package com.mindset.data

import com.mindset.data.remote.WgerApi
import com.mindset.domain.MuscleImageProvider
import com.mindset.model.MuscleDiagram
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Resolves a free-exercise-db muscle name to a wger [MuscleDiagram] (base body SVG + highlighted-
 * muscle overlay SVG, layered by the UI). wger's per-muscle image is only the overlay, so we pair it
 * with the front/back base silhouette. The `/muscle/` list is fetched once and memoized; overlay URLs
 * are content-hashed on wger (fetched live, not hardcoded). Muscles with no wger equivalent → null.
 */
class MuscleImageProviderImpl(private val wgerApi: WgerApi) : MuscleImageProvider {

    private data class Overlay(val url: String, val isFront: Boolean)

    private val mutex = Mutex()
    private var byWgerId: Map<Int, Overlay>? = null

    override suspend fun diagram(muscleName: String): MuscleDiagram? {
        val wgerId = OUR_TO_WGER[muscleName.lowercase()] ?: return null
        val overlay = loadOnce()[wgerId] ?: return null
        val base = if (overlay.isFront) BASE_FRONT else BASE_BACK
        return MuscleDiagram(baseUrl = base, overlayUrl = overlay.url)
    }

    private suspend fun loadOnce(): Map<Int, Overlay> {
        byWgerId?.let { return it }
        return mutex.withLock {
            byWgerId ?: wgerApi.muscles()
                .mapNotNull { m -> m.imageUrlMain?.let { m.id to Overlay(it, m.isFront) } }
                .toMap()
                .also { byWgerId = it }
        }
    }

    private companion object {
        const val BASE_FRONT = "https://wger.de/static/images/muscles/muscular_system_front.svg"
        const val BASE_BACK = "https://wger.de/static/images/muscles/muscular_system_back.svg"

        /** free-exercise-db muscle → stable wger muscle id (ids stable; URLs content-hashed). */
        val OUR_TO_WGER = mapOf(
            "abdominals" to 6, "biceps" to 1, "calves" to 7, "chest" to 4, "glutes" to 8,
            "hamstrings" to 11, "lats" to 12, "middle back" to 9, "quadriceps" to 10,
            "shoulders" to 2, "traps" to 9, "triceps" to 5,
        )
    }
}
