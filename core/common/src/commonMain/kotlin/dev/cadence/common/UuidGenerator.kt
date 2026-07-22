package dev.cadence.common

/**
 * Generates string ids for new persistable rows.
 *
 * A seam — not a bare `Uuid.random()` at each call site — so identity generation is injectable:
 * production binds [UuidV7Generator]; tests substitute a deterministic generator. Returns the
 * canonical lowercase `8-4-4-4-12` hex form.
 */
interface UuidGenerator {
    fun newId(): String
}
