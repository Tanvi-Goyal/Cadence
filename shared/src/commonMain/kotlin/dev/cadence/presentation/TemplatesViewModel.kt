package dev.cadence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cadence.data.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One template row for the Templates list. */
data class TemplateUi(
    val id: String,
    val name: String,
    val type: String,
)

/**
 * Drives the Templates screen. Templates are ordinary [dev.cadence.data.local.Session] rows with
 * `isTemplate = true`; the list observes the DB (never the network). Creating a template and
 * starting one both hand an id back via a callback so the UI can navigate.
 */
class TemplatesViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<List<TemplateUi>> =
        repository.observeTemplates()
            .map { templates -> templates.map { TemplateUi(it.id, it.name, it.type) } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    /** Create an empty template, then hand its id back to open the builder. */
    fun createTemplate(name: String, type: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val template = repository.createTemplate(name = name, type = type)
            onCreated(template.id)
        }
    }

    /** Instantiate a template into a real session (deep copy), then open it in Log Workout. */
    fun startTemplate(templateId: String, onStarted: (String) -> Unit) {
        viewModelScope.launch {
            val session = repository.instantiateTemplate(templateId)
            onStarted(session.id)
        }
    }
}
