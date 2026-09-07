@file:OptIn(ExperimentalTime::class)

package com.mindset.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The sync envelope every user-owned entity carries.
 *
 * Present on the domain model (not just the DB row) because sync is a first-class product concern:
 * [updatedAt] is the Last-Write-Wins conflict key and [deletedAt] is a tombstone — a soft delete
 * that can still propagate to other devices (a hard-deleted row can't be synced once it's gone).
 * `deletedAt = null` means the row is live. The local-only `syncStatus` (is this row pushed yet?)
 * deliberately lives on the Room entity, never here — it has no meaning in the domain or on the wire.
 */
interface Syncable {
    /** Client-generated UUIDv7 (see `UuidV7Generator`); legacy rows keep their original v4. */
    val id: String
    val createdAt: Instant
    val updatedAt: Instant
    val deletedAt: Instant?
}
