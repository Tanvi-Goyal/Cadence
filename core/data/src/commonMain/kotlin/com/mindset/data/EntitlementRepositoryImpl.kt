package com.mindset.data

import com.mindset.data.local.AppDatabase
import com.mindset.data.local.EntitlementEntity
import com.mindset.domain.Entitlement
import com.mindset.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed stub [EntitlementRepository] for v1 — the app writes [setPro] directly (a dev toggle in
 * Profile). A RevenueCat-backed impl later replaces this behind the same interface.
 */
class EntitlementRepositoryImpl(private val database: AppDatabase) : EntitlementRepository {
    private val dao get() = database.entitlementDao()

    override fun observe(): Flow<Entitlement> = dao.observe().map { Entitlement(isPro = it?.isPro ?: false) }

    override suspend fun setPro(isPro: Boolean) = dao.upsert(EntitlementEntity(isPro = isPro))
}
