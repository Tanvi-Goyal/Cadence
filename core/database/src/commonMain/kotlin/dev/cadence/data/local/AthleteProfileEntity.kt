package dev.cadence.data.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "athlete_profile")
data class AthleteProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val fullName: String,
    val bodyweightKg: Double?,
    val heightCm: Double?,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}