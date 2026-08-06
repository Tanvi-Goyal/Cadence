package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.repository.AthleteProfileRepository
import com.mindset.domain.LogSectionUi
import com.mindset.domain.repository.SessionRepository
import com.mindset.domain.deriveSessionType
import com.mindset.domain.toLogSections
import com.mindset.model.HyroxStepDef
import com.mindset.model.SessionType
import com.mindset.model.SetEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One selectable Hyrox station for the "Add Exercise or Station" sheet. */
data class StationOption(
    val segmentKey: String,
    val name: String,   // e.g. "2. Sled Push"
    val standard: String, // division-accurate readout, e.g. "152 kg" / "1000m" / "100 reps"
)

data class LogWorkoutUiState(
    val sessionName: String = "",
    /** Auto-derived from what's logged; shown as a read-only header tag, persisted on Complete. */
    val sessionType: SessionType = SessionType.STRENGTH,
    val startedAtMillis: Long = 0L,
    val notes: String = "",
    val sections: List<LogSectionUi> = emptyList(),
)

/**
 * Drives the Log Session screen for one [sessionId]. Reads the hydrated session from the DB (never the
 * network) as ordered blocks/sections; writes go through the repository, which bumps the parent session
 * so the whole aggregate re-syncs (and runs PB detection on real actuals). The session type is derived
 * live from content ([deriveSessionType]) rather than picked, and persisted when the user Completes.
 */
@OptIn(FlowPreview::class)
class LogWorkoutViewModel(
    private val repository: SessionRepository,
    private val athleteProfile: AthleteProfileRepository,
    private val sessionId: String,
) : ViewModel() {

    val uiState: StateFlow<LogWorkoutUiState> =
        repository.observeSessionDetail(sessionId).map { detail ->
            LogWorkoutUiState(
                sessionName = detail?.session?.name.orEmpty(),
                sessionType = deriveSessionType(detail),
                startedAtMillis = detail?.session?.startedAt?.toEpochMilliseconds() ?: 0L,
                notes = detail?.session?.notes.orEmpty(),
                sections = detail.toLogSections(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LogWorkoutUiState(),
        )

    /**
     * The 8 Hyrox stations for the athlete's default division — the "Stations" section of the add
     * sheet. Kept OFF [uiState] so seeding the reference tables never delays the visible session content.
     */
    val stations: StateFlow<List<StationOption>> =
        athleteProfile.observe()
            .map { it.defaultDivisionKey }
            .distinctUntilChanged()
            .map { division -> repository.hyroxStations(division).map { it.toStationOption() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Reference "standard" labels per station `segmentKey` for the athlete's gender (both tiers) + race
     * mode — powers the separate "Standard" view on each station card. Off [uiState] for the same reason
     * as [stations].
     */
    val stationStandards: StateFlow<Map<String, String>> =
        athleteProfile.observe()
            .map { it.defaultDivisionKey to (it.defaultMode ?: "SINGLES") }
            .distinctUntilChanged()
            .map { (division, mode) -> repository.stationStandardLabels(division, mode) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Note edits are coalesced: the field updates instantly in the UI; the DB write lands after a pause.
    private val notesInput = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        notesInput
            .debounce(400)
            .onEach { repository.updateSessionNotes(sessionId, it) }
            .launchIn(viewModelScope)
    }

    /** Add an exercise and prefill its sets from the last time it was logged (ghost targets). */
    fun addExercisePrefilled(exerciseId: String) {
        viewModelScope.launch { repository.addExercisePrefilled(sessionId, exerciseId) }
    }

    /** Add a Hyrox station, seeding its division-standard ghost target. */
    fun addStation(segmentKey: String) {
        viewModelScope.launch {
            val division = athleteProfile.observe().first().defaultDivisionKey
            repository.addStation(sessionId, division, segmentKey)
        }
    }

    /** Append a new actual set (beyond the prescription) — metric-agnostic; pass only the relevant cells. */
    fun addSet(loggedItemId: String, reps: Int? = null, loadKg: Double? = null, timeSec: Int? = null, distanceM: Int? = null) {
        viewModelScope.launch {
            repository.addSet(sessionId, loggedItemId, reps = reps, loadKg = loadKg, timeSec = timeSec, distanceM = distanceM)
        }
    }

    /** Persist a set's actuals against its (possibly ghost) target. The screen builds the metric-specific copy. */
    fun updateActual(set: SetEntry) {
        viewModelScope.launch { repository.updateSet(sessionId, set) }
    }

    /** Coalesced note write (see [notesInput]). */
    fun onNotesChange(notes: String) {
        notesInput.tryEmit(notes)
    }

    /** Remove an exercise/station from the session. */
    fun removeEntry(entryId: String) {
        viewModelScope.launch { repository.removeEntry(sessionId, entryId) }
    }

    /** Complete the session: stamp finished + persist the auto-derived type, then hand control back. */
    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.finishSession(sessionId, uiState.value.sessionType)
            onDone()
        }
    }
}

private fun HyroxStepDef.toStationOption(): StationOption =
    StationOption(segmentKey = segmentKey.orEmpty(), name = title, standard = value)
