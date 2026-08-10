package com.mindset.presentation

import com.mindset.model.RaceGoal
import com.mindset.model.Session

data class HomeUiState(val raceInfo: RaceGoal? = null, val sessions: List<Session> = emptyList())
