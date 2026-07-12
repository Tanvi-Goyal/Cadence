package dev.cadence.contracts

import kotlinx.serialization.Serializable

/**
 * The wire representation of a session — the sync contract shared by client and server.
 *
 * Note what's absent: `syncStatus`. That's a purely local concern (is this row pushed yet?) and
 * has no meaning on the wire, so it never crosses the network. `updatedAt` DOES cross — it's the
 * clock the Last-Write-Wins conflict rule compares. `deleted` crosses too, because deletions must
 * propagate: a hard delete can't be synced once the row is gone (hence soft delete).
 */
@Serializable
data class SessionDto(
    val id: String,
    val startedAt: Long,
    val notes: String? = null,
    val updatedAt: Long,
    val deleted: Boolean = false,
)
