package com.mindset.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.mindset.data.local.EntitlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitlementDao {
    @Query("SELECT * FROM entitlement WHERE id = 0")
    fun observe(): Flow<EntitlementEntity?>

    @Upsert
    suspend fun upsert(entitlement: EntitlementEntity)
}
