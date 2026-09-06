package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.repository.SessionRepository
import com.mindset.model.SessionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the session the Log **tab** is editing.
 *
 * The tab has no route argument to carry a session id, so it resolves one itself:
 * [SessionRepository.resumeOrCreateSession] hands back the newest unfinished hand-logged draft, or
 * mints one. That is what makes the tab behave like a place — switching tabs, backgrounding, even a
 * process death all return to the same half-filled workout instead of a blank new row each time.
 *
 * [sessionId] is null only for the first frame, while the DB read is in flight.
 */
class LogTabViewModel(private val repository: SessionRepository) : ViewModel() {
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    init {
        resolve()
    }

    /**
     * Intent: the workout was completed. The finished session is no longer a draft, so the tab drops
     * it and resolves a fresh one — otherwise coming back to Log would reopen a session that is
     * already in History.
     */
    fun onCompleted() {
        _sessionId.value = null
        resolve()
    }

    private fun resolve() {
        viewModelScope.launch {
            _sessionId.value = repository.resumeOrCreateSession(SessionType.STRENGTH.name).id
        }
    }
}
