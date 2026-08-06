package dev.cadence.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Single-row table holding the user's preferences. Device-local — like [Exercise] and [SyncMeta] it
 * has no sync fields and never enqueues an outbox row, so it is not part of the sync graph. Enum
 * values are stored as their `.name` strings (plain TEXT, no Room type converter).
 */
@Entity(tableName = "preferences")
data class PreferencesEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val weightUnit: String,
    val themeMode: String,
) {
    companion object {
        /** There is only ever one preferences row. */
        const val SINGLETON_ID = 0
    }
}
