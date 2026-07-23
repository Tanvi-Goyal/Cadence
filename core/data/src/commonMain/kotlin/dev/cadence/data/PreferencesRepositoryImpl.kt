package dev.cadence.data

import dev.cadence.data.local.AppDatabase
import dev.cadence.data.local.PreferencesEntity
import dev.cadence.domain.PreferencesRepository
import dev.cadence.domain.ThemeMode
import dev.cadence.domain.UserPreferences
import dev.cadence.domain.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Room-backed [PreferencesRepository]. Plain constructor injection, matching [SessionRepositoryImpl]. */
class PreferencesRepositoryImpl(
    private val database: AppDatabase,
) : PreferencesRepository {

    private val dao get() = database.preferencesDao()

    override fun observe(): Flow<UserPreferences> =
        dao.observe().map { it?.toDomain() ?: UserPreferences() }

    override suspend fun setWeightUnit(unit: WeightUnit) = update { it.copy(weightUnit = unit) }

    override suspend fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    private suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        val current = dao.observe().first()?.toDomain() ?: UserPreferences()
        dao.upsert(transform(current).toEntity())
    }
}

private fun PreferencesEntity.toDomain() = UserPreferences(
    weightUnit = runCatching { WeightUnit.valueOf(weightUnit) }
        .getOrDefault(WeightUnit.KG),
    themeMode = runCatching { ThemeMode.valueOf(themeMode) }
        .getOrDefault(ThemeMode.SYSTEM),
)

private fun UserPreferences.toEntity() = PreferencesEntity(
    weightUnit = weightUnit.name,
    themeMode = themeMode.name,
)
