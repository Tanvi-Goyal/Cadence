package dev.cadence.domain

import kotlinx.coroutines.flow.Flow

/** The user's subscription entitlement. [isPro] unlocks the Pro-gated surfaces (full History, etc.). */
data class Entitlement(val isPro: Boolean = false)

/**
 * The single seam the app reads subscription state through. For v1 the impl is a local stub driven by a
 * dev toggle; a RevenueCat-backed impl later replaces it behind this same interface — consumers (History
 * paywall, Profile subscription card) depend only on this, so nothing downstream changes.
 */
interface EntitlementRepository {
    /** Emits the current entitlement, defaulting to non-Pro when nothing has been written. */
    fun observe(): Flow<Entitlement>

    /** Dev/stub setter. The RevenueCat impl will instead sync this from the purchase state. */
    suspend fun setPro(isPro: Boolean)
}
