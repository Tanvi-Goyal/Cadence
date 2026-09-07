@file:OptIn(ExperimentalTime::class)

package com.mindset.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mindset.domain.repository.ActiveRaceSnapshot
import com.mindset.domain.repository.ActiveRaceStore
import com.mindset.model.Gender
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [ActiveRaceStore] over the app's existing preferences [DataStore] — the same instance
 * [PreferencesRepositoryImpl] uses, and the same idioms (a private `Keys` object, enums stored by
 * `.name` and read back defensively).
 *
 * Sharing that store rather than opening a second one is deliberate: a second file would need a new
 * path function in both `androidMain` and `iosMain` of `:core:datastore`, i.e. a new platform seam,
 * for a record written roughly once every ten seconds while racing.
 *
 * A single `edit {}` applies as one atomic snapshot, so the record can never be observed half-written
 * — which matters because these fields are only meaningful together: a `currentIndex` paired with the
 * wrong `splitElapsedMs` would restore a plausible-looking but wrong race.
 */
internal class ActiveRaceStoreImpl(private val dataStore: DataStore<Preferences>) : ActiveRaceStore {

    override suspend fun read(): ActiveRaceSnapshot? {
        val prefs = dataStore.data.firstOrNull() ?: return null
        // Every field is required. A record missing any of them — or carrying an enum this build no
        // longer knows — is treated as absent rather than half-built into a snapshot.
        val sessionId = prefs[Keys.SESSION_ID] ?: return null
        val divisionKey = prefs[Keys.DIVISION_KEY] ?: return null
        val variant = prefs[Keys.VARIANT]?.let { it.toEnumOrNull(HyroxVariant.entries) } ?: return null
        val raceMode = prefs[Keys.RACE_MODE]?.let { it.toEnumOrNull(RaceMode.entries) } ?: return null
        val gender = prefs[Keys.GENDER]?.let { it.toEnumOrNull(Gender.entries) } ?: return null
        val currentIndex = prefs[Keys.CURRENT_INDEX] ?: return null
        val totalElapsedMs = prefs[Keys.TOTAL_ELAPSED_MS] ?: return null
        val splitElapsedMs = prefs[Keys.SPLIT_ELAPSED_MS] ?: return null
        val paused = prefs[Keys.PAUSED] ?: return null
        val snapshotAt = prefs[Keys.SNAPSHOT_AT] ?: return null

        return ActiveRaceSnapshot(
            sessionId = sessionId,
            divisionKey = divisionKey,
            variant = variant,
            raceMode = raceMode,
            gender = gender,
            currentIndex = currentIndex,
            totalElapsedMs = totalElapsedMs,
            splitElapsedMs = splitElapsedMs,
            paused = paused,
            snapshotAt = Instant.fromEpochMilliseconds(snapshotAt),
        )
    }

    override suspend fun save(snapshot: ActiveRaceSnapshot) {
        dataStore.edit { prefs ->
            prefs[Keys.SESSION_ID] = snapshot.sessionId
            prefs[Keys.DIVISION_KEY] = snapshot.divisionKey
            prefs[Keys.VARIANT] = snapshot.variant.name
            prefs[Keys.RACE_MODE] = snapshot.raceMode.name
            prefs[Keys.GENDER] = snapshot.gender.name
            prefs[Keys.CURRENT_INDEX] = snapshot.currentIndex
            prefs[Keys.TOTAL_ELAPSED_MS] = snapshot.totalElapsedMs
            prefs[Keys.SPLIT_ELAPSED_MS] = snapshot.splitElapsedMs
            prefs[Keys.PAUSED] = snapshot.paused
            prefs[Keys.SNAPSHOT_AT] = snapshot.snapshotAt.toEpochMilliseconds()
        }
    }

    override suspend fun clear() {
        // Remove only this feature's keys — the store is shared with the athlete's preferences.
        dataStore.edit { prefs ->
            prefs.remove(Keys.SESSION_ID)
            prefs.remove(Keys.DIVISION_KEY)
            prefs.remove(Keys.VARIANT)
            prefs.remove(Keys.RACE_MODE)
            prefs.remove(Keys.GENDER)
            prefs.remove(Keys.CURRENT_INDEX)
            prefs.remove(Keys.TOTAL_ELAPSED_MS)
            prefs.remove(Keys.SPLIT_ELAPSED_MS)
            prefs.remove(Keys.PAUSED)
            prefs.remove(Keys.SNAPSHOT_AT)
        }
    }

    private object Keys {
        val SESSION_ID = stringPreferencesKey("active_race_session_id")
        val DIVISION_KEY = stringPreferencesKey("active_race_division_key")
        val VARIANT = stringPreferencesKey("active_race_variant")
        val RACE_MODE = stringPreferencesKey("active_race_mode")
        val GENDER = stringPreferencesKey("active_race_gender")
        val CURRENT_INDEX = intPreferencesKey("active_race_index")
        val TOTAL_ELAPSED_MS = longPreferencesKey("active_race_total_ms")
        val SPLIT_ELAPSED_MS = longPreferencesKey("active_race_split_ms")
        val PAUSED = booleanPreferencesKey("active_race_paused")
        val SNAPSHOT_AT = longPreferencesKey("active_race_snapshot_at")
    }
}

/** Enum-by-name lookup that survives a stored value this build no longer defines. */
private fun <T : Enum<T>> String.toEnumOrNull(entries: List<T>): T? = entries.firstOrNull { it.name == this }
