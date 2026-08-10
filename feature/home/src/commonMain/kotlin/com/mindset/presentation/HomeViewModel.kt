package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mindset.domain.ActiveWorkoutController
import com.mindset.domain.repository.RaceGoalRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repository: SessionRepository,
    private val activeWorkoutController: ActiveWorkoutController,
    private val raceGoalRepository: RaceGoalRepository,
) : ViewModel() {
//
//    val activeWorkout: StateFlow<ActiveWorkout?> get() = activeWorkoutController.state
//
//    private val _openSession = MutableSharedFlow<String>(extraBufferCapacity = 1)
//    val openSession: SharedFlow<String> = _openSession.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedSessions: Flow<PagingData<Session>> = repository.pagedSessions().cachedIn(viewModelScope)

    //    private val homeData: Flow<HomeData> =
//        combine(
//            repository.observeSessions(),
//            repository.observePlannedSession(),
//            repository.observeVolumesBySession(),
//            repository.observeTemplates(),
//        ) { sessions, planned, volumes, templates ->
//            HomeData(sessions, planned, volumes, templates)
//        }
//
    val uiState: StateFlow<HomeUiState> = raceGoalRepository.observeUpcoming().map { raceInfo ->
        HomeUiState(
            raceInfo = raceInfo,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onExpandWorkout() {
        activeWorkoutController.expand()
    }

    fun onResetWorkout() {
        activeWorkoutController.reset()
    }

    fun onToggleWorkoutPause() {
        val workout = activeWorkoutController.state.value ?: return
        if (workout.paused) activeWorkoutController.resume() else activeWorkoutController.pause()
    }

    fun onAdvanceWorkout() {
        val workout = activeWorkoutController.state.value ?: return
        val wasLast = workout.currentIndex >= workout.totalSteps - 1
        activeWorkoutController.next()
        if (wasLast) activeWorkoutController.expand()
    }
}
