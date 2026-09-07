package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Single-row table holding the user's subscription entitlement. Device-local (no sync fields). For v1
 * this is a stub the app writes directly (a dev toggle); the RevenueCat SDK will later own the source
 * of truth behind the same `EntitlementRepository` seam and simply update this cached row.
 */
@Entity(tableName = "entitlement")
data class EntitlementEntity(@PrimaryKey val id: Int = SINGLETON_ID, val isPro: Boolean) {
    companion object {
        /** There is only ever one entitlement row. */
        const val SINGLETON_ID = 0
    }
}
