@file:OptIn(ExperimentalTime::class)

package com.mindset.domain.repository

import com.mindset.model.Gender
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The timing state of a live race, as last observed. Enough to rebuild the race after the process
 * dies — everything else is either re-derivable (`steps`, from the four format fields) or already in
 * the database (the session row and its splits).
 *
 * [totalElapsedMs] and [splitElapsedMs] are **live-inclusive**: they include the running segment as
 * of [snapshotAt], not just what was banked at the last pause/advance. That is what lets a restore
 * pick up near where the race actually was rather than rewinding to the start of the current step.
 */
data class ActiveRaceSnapshot(
    val sessionId: String,
    val divisionKey: String,
    val variant: HyroxVariant,
    /**
     * Persisted rather than re-read from preferences so the `steps` rebuild reproduces the *original*
     * call: changing division or mode in Profile mid-race would otherwise rebuild a list whose
     * weights disagree with the ones already seeded into this session's sets.
     */
    val raceMode: RaceMode,
    val gender: Gender,
    val currentIndex: Int,
    val totalElapsedMs: Long,
    val splitElapsedMs: Long,
    val paused: Boolean,
    /** When the app last *observed* this race — the clock the staleness cutoff is measured against. */
    val snapshotAt: Instant,
)

/**
 * Device-local storage for the one in-flight race, so it survives process death.
 *
 * Deliberately separate from [PreferencesRepository]: that models choices the athlete made and is
 * surfaced as an observable `UserPreferences`, whereas this is ephemeral machine state that must
 * never appear there. Deliberately not a database table either — these anchors are device-local and
 * must never sync, and a Room table would sit next to synced entities inviting exactly that.
 */
interface ActiveRaceStore {
    /** The stored race, or null when there is none (or the record is incomplete/unparseable). */
    suspend fun read(): ActiveRaceSnapshot?

    /** Writes the whole record atomically — a partial record must never be observable. */
    suspend fun save(snapshot: ActiveRaceSnapshot)

    /**
     * Drops the record. Called when a race finishes or is dismissed — without this a completed race
     * resurrects on the next cold start.
     */
    suspend fun clear()
}
