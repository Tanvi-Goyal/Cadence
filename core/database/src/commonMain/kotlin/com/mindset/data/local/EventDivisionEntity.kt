package com.mindset.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "event_division", indices = [Index("formatKey")])
data class EventDivisionEntity(
    @PrimaryKey val id: String, // "HYROX:MEN"
    val formatKey: String,
    val key: String, // WOMEN / MEN / WOMEN_PRO / MEN_PRO (stored on sessions/goals/PRs)
    val label: String,
    val gender: String, // com.mindset.model.Gender name
    val tier: String, // com.mindset.model.Tier name
    val orderIndex: Int,
)
