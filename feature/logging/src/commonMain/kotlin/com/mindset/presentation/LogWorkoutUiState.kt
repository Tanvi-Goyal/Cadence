package com.mindset.presentation

import com.mindset.domain.LogSectionUi
import com.mindset.model.SessionType

data class LogWorkoutUiState(
    val sessionName: String,
    val sessionType: SessionType = SessionType.STRENGTH,
    val startedAtMillis: Long,
    val notes: String,
    val sections: List<LogSectionUi> = emptyList(),
)
