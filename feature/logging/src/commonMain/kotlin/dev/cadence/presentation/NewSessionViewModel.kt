package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.domain.SessionRepository
import kotlinx.coroutines.launch

/** New Session screen: creates a session of the chosen type, then hands back its id to navigate. */
class NewSessionViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    fun create(type: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val session = repository.createSession(type)
            onCreated(session.id)
        }
    }
}
