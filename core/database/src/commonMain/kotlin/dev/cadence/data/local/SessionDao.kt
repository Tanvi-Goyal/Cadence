package dev.cadence.data.local

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow

/**
 * Reads/writes for [Session]. [observeAll] returns a [Flow] so the UI observes the DB reactively
 * and never has to poll — the single-source-of-truth contract in AGENTS.md. The class-level converter
 * lets a [PagingSource]-returning query compile in commonMain (Room-KMP requirement, same as ExerciseDao).
 */
@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE deletedAt IS NULL AND isTemplate = 0 ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<Session>>

    /**
     * The reverse-chronological session feed as a Room [PagingSource] — the History (Pro) list.
     * Same predicate/ordering as [observeAll]; [type] narrows by [SessionType] (null = all), guarded
     * the same way as the exercise search query.
     */
    @Query(
        """
        SELECT * FROM sessions
        WHERE deletedAt IS NULL AND isTemplate = 0 AND (:type IS NULL OR type = :type)
        ORDER BY startedAt DESC
        """,
    )
    fun pagedSessions(type: String?): PagingSource<Int, Session>

    /** Templates only (D2) — for the template picker. Ordered by name since they have no real time. */
    @Query("SELECT * FROM sessions WHERE deletedAt IS NULL AND isTemplate = 1 ORDER BY name")
    fun observeTemplates(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun observeById(id: String): Flow<Session?>

    @Insert
    suspend fun insert(session: Session)

    /** Insert-or-replace, used by the pull path to apply a remote version of a row. */
    @Upsert
    suspend fun upsert(session: Session)

    /** Read one row (may be soft-deleted) — the sync engine needs it to apply Last-Write-Wins. */
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): Session?

    /** Mark a set of sessions as synced after a successful push. */
    @Query("UPDATE sessions SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun markStatus(ids: List<String>, status: String)

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int
}
