package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.deriveSessionType
import com.mindset.domain.repository.AthleteProfileRepository
import com.mindset.domain.repository.PreferencesRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.domain.toLogSections
import com.mindset.model.HyroxStationModel
import com.mindset.model.SessionType
import com.mindset.model.SetEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class LogWorkoutViewModel(
    private val repository: SessionRepository,
    private val athleteProfile: AthleteProfileRepository,
    private val preferencesRepository: PreferencesRepository,
    private val sessionId: String,
) : ViewModel() {
    val uiState: StateFlow<LogWorkoutUiState> =
        repository
            .observeSessionDetail(sessionId)
            .map { detail ->
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
                initialValue =
                LogWorkoutUiState(
                    sessionName = "",
                    sessionType = SessionType.STRENGTH,
                    startedAtMillis = 0L,
                    notes = "",
                    sections = emptyList(),
                ),
            )

    val stations: StateFlow<List<StationOption>> =
        combine(preferencesRepository.observe(), athleteProfile.observe()) { prefs, _ ->
            val divisionKey = prefs.hyroxDivisionKey
            val raceMode = prefs.raceMode
            val gender = prefs.gender

            repository
                .hyroxStations(divisionKey!!, raceMode!!, gender!!)
                .map { it.toStationOption() }
                .filterNot { station -> divisionKey != null && station.segmentKey == null }
                .map { station ->
                    if (station.segmentKey != null) {
                        val standard =
                            repository.stationStandardLabels(
                                divisionKey,
                                raceMode,
                                gender,
                            )[station.segmentKey]
                        station.copy(standard = standard!!)
                    } else {
                        station
                    }
                }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    /**
     * Reference "standard" labels per station `segmentKey` for the athlete's gender (both tiers) + race
     * mode — powers the separate "Standard" view on each station card. Off [uiState] for the same reason
     * as [stations].
     */
    val stationStandards: StateFlow<Map<String, String>> =
        preferencesRepository
            .observe()
            .distinctUntilChanged()
            .map { prefs ->
                repository.stationStandardLabels(
                    prefs.hyroxDivisionKey!!,
                    prefs.raceMode!!,
                    prefs.gender!!,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyMap(),
            )

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

    fun addStation(segmentKey: String) {
        viewModelScope.launch {
            val raceInfo = preferencesRepository.getRaceInfo()
            repository.addStation(
                sessionId = sessionId,
                divisionKey = raceInfo.first!!,
                segmentKey = segmentKey,
                raceMode = raceInfo.third!!,
                gender = raceInfo.second!!,
            )
        }
    }

    /** Persist a set's actuals against its (possibly ghost) target. The screen builds the metric-specific copy. */
    fun updateActual(set: SetEntry) {
        viewModelScope.launch { repository.updateSet(sessionId, set) }
    }

    fun onNotesChange(notes: String) {
        notesInput.tryEmit(notes)
    }

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

private fun HyroxStationModel.toStationOption(): StationOption = StationOption(segmentKey = segmentKey.orEmpty(), name = title, standard = value)
