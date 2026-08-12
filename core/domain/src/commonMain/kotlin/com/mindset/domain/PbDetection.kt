package com.mindset.domain

import com.mindset.model.Exercise
import com.mindset.model.MetricType
import com.mindset.model.PersonalRecord
import com.mindset.model.PrKind
import com.mindset.model.SetEntry

/**
 * A personal record that a completed set would set, without identity/timestamps — the pure output of
 * [detectPrs]. The repository turns each candidate into a cached `personal_record` row (assigning an
 * id + timestamps, or updating the existing row of the same kind/bucket).
 */
data class PrCandidate(
    val kind: PrKind,
    val value: Double,
    val distanceBucketM: Int?,
    val divisionKey: String? = null,
)

/**
 * Epley estimated one-rep max: `weight × (1 + reps/30)`. Deliberately one line and defensible in an
 * interview (and to a training partner) — MindSet prefers an explainable heuristic over a black box.
 */
fun epleyOneRepMax(
    loadKg: Double,
    reps: Int,
): Double = loadKg * (1 + reps / 30.0)

/**
 * The distance a time PR is filed under. v1 keys on the exact metres logged (so "best 1000 m" is its
 * own record); snapping near-misses (1005 m → 1000 m) to standard distances is a future refinement.
 */
fun distanceBucketFor(
    distanceM: Int,
): Int = distanceM

/**
 * Pure PB detection (LLD §7.2). Given the just-completed [set]'s actuals, its [exercise], the
 * currently cached [current] records, and the set's [divisionKey] (weight class — null for
 * division-agnostic work), return the records that were **strictly** beaten — an equal result is not
 * a new PR. No id/timestamp work here (kept pure + fully unit-testable); the repo persists the
 * winners. A metric's PRs are produced only when the actuals it needs are present, so template
 * (target-only) sets naturally yield nothing.
 *
 * PBs are compared **within a division bucket**: "Sled Push @ Men" and "@ Women Pro" are independent
 * records, so a lighter-class time never overwrites a heavier-class one (and vice-versa).
 */
fun detectPrs(
    exercise: Exercise,
    set: SetEntry,
    current: List<PersonalRecord>,
    divisionKey: String? = null,
): List<PrCandidate> {
    fun current(kind: PrKind, bucket: Int? = null): Double? = current.firstOrNull {
        it.kind == kind && it.distanceBucketM == bucket && it.divisionKey == divisionKey
    }?.value

    // "First record of its kind (in this division bucket) always counts; otherwise must strictly beat it."
    fun beatsHigher(
        value: Double,
        kind: PrKind,
        bucket: Int? = null
    ): Boolean = current(kind, bucket)?.let { value > it } ?: true

    fun beatsLower(
        value: Double,
        kind: PrKind, bucket: Int? = null
    ): Boolean = current(kind, bucket)?.let { value < it } ?: true

    return when (exercise.defaultMetric) {
        MetricType.WEIGHT_REPS -> buildList {
            val reps = set.reps
            val load = set.loadKg
            if (reps != null && reps > 0 && load != null) {
                val e1rm = epleyOneRepMax(load, reps)
                if (beatsHigher(e1rm, PrKind.EST_1RM))
                    add(PrCandidate(PrKind.EST_1RM, e1rm, null, divisionKey))
                if (beatsHigher(load, PrKind.MAX_WEIGHT))
                    add(PrCandidate(PrKind.MAX_WEIGHT, load, null, divisionKey))
            }
        }

        MetricType.REPS_ONLY -> buildList {
            val reps = set.reps
            if (reps != null && beatsHigher(reps.toDouble(), PrKind.MAX_REPS)) {
                add(PrCandidate(PrKind.MAX_REPS, reps.toDouble(), null, divisionKey))
            }
        }

        MetricType.DISTANCE_TIME -> buildList {
            val dist = set.distanceM
            val time = set.timeSec
            if (dist != null && dist > 0 && time != null && time > 0) {
                val bucket = distanceBucketFor(dist)
                if (beatsLower(time.toDouble(), PrKind.BEST_TIME, bucket)) {
                    add(PrCandidate(PrKind.BEST_TIME, time.toDouble(), bucket, divisionKey))
                }
            }
        }

        // Fixed-rep work scored by time (Wall Balls): the PB is the best time, bucketed by the rep
        // count so a 100-rep effort and a 50-rep effort are separate records.
        MetricType.REPS_TIME -> buildList {
            val reps = set.reps
            val time = set.timeSec
            if (reps != null && reps > 0 && time != null && time > 0) {
                if (beatsLower(time.toDouble(), PrKind.BEST_TIME, reps)) {
                    add(PrCandidate(PrKind.BEST_TIME, time.toDouble(), reps, divisionKey))
                }
            }
        }

        MetricType.CALORIES -> buildList {
            val cal = set.calories
            if (cal != null && beatsHigher(cal.toDouble(), PrKind.MAX_CALORIES)) {
                add(PrCandidate(PrKind.MAX_CALORIES, cal.toDouble(), null, divisionKey))
            }
        }

        // Longest-hold PRs are configurable per exercise later (LLD §7.2); no default record for now.
        MetricType.DURATION -> emptyList()
    }
}
