package com.mindset.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Read-only client for the public wger.de exercise API (CC-BY-SA data). Only the muscle diagram
 * images are consumed today. Reuses the shared Ktor [HttpClient] (JSON negotiation already set up).
 * Fails soft (empty) — enrichment is best-effort.
 */
class WgerApi(private val client: HttpClient) {

    suspend fun muscles(): List<WgerMuscle> =
        runCatching {
            client.get("$BASE/muscle/?format=json&limit=50").body<WgerMuscleResponse>().results
        }.getOrElse { emptyList() }

    private companion object {
        const val BASE = "https://wger.de/api/v2"
    }
}

@Serializable
data class WgerMuscleResponse(val results: List<WgerMuscle> = emptyList())

/** A wger muscle. `image_url_main` is a highlighted body-silhouette SVG (CC-BY-SA). */
@Serializable
data class WgerMuscle(
    val id: Int,
    @SerialName("name_en") val nameEn: String = "",
    val name: String = "",
    @SerialName("is_front") val isFront: Boolean = true,
    @SerialName("image_url_main") val imageUrlMain: String? = null,
)
