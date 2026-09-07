package com.mindset.model

data class EventDivision(
    val id: String,
    val formatKey: String,
    val key: String,
    val label: String,
    val gender: Gender,
    val tier: Tier,
    val orderIndex: Int,
)
