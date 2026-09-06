package com.mindset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindset.domain.Units
import com.mindset.domain.WeightUnit
import com.mindset.domain.deriveSessionType
import com.mindset.domain.digitsToSeconds
import com.mindset.domain.repository.AthleteProfileRepository
import com.mindset.domain.repository.PreferencesRepository
import com.mindset.domain.repository.SessionRepository
import com.mindset.domain.secondsToDigits
import com.mindset.domain.toLogSections
import com.mindset.model.Gender
import com.mindset.model.HyroxStationModel
import com.mindset.model.HyroxVariant
import com.mindset.model.RaceMode
import com.mindset.model.SessionType
import com.mindset.model.SetEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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
            val race = raceIdentity(prefs.hyroxDivisionKey, prefs.gender, prefs.raceMode)

            // Hoisted out of the per-station map below: the labels are keyed by division/mode/gender,
            // not by station, so calling it inside the map was one DB round trip per station.
            val standards =
                repository.stationStandardLabels(race.divisionKey, race.raceMode, race.gender)

            repository
                .hyroxStations(race.divisionKey, race.raceMode, race.gender)
                .map { it.toStationOption() }
                .map { station ->
                    station.copy(standard = standards[station.segmentKey] ?: station.standard)
                }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    val stationStandards: StateFlow<Map<String, String>> =
        preferencesRepository
            .observe()
            .distinctUntilChanged()
            .map { prefs ->
                val race = raceIdentity(prefs.hyroxDivisionKey, prefs.gender, prefs.raceMode)
                repository.stationStandardLabels(race.divisionKey, race.raceMode, race.gender)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyMap(),
            )

    private val notesInput = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val _drafts = MutableStateFlow<Map<String, StationDraft>>(emptyMap())
    val drafts: StateFlow<Map<String, StationDraft>> = _drafts

    // Display unit for `load` (stored canonically in kg). Snapshotted for the synchronous commit path.
    private var currentUnit: WeightUnit = WeightUnit.KG

    // Newest note the user typed, ahead of the debounced DB write. Null = never edited this session.
    private var lastNotes: String? = null

    // Both exits suspend before navigating, so guard against a second tap (or back) re-entering them.
    private var exiting = false

    init {
        notesInput
            .debounce(400.milliseconds)
            .onEach { repository.updateSessionNotes(sessionId, it) }
            .launchIn(viewModelScope)

        combine(
            uiState.map { state ->
                state.sections
                    .flatMap { it.items }
                    .flatMap { it.sets }
            }
                .distinctUntilChanged(),
            preferencesRepository.observe()
                .map { it.weightUnit }
                .distinctUntilChanged(),
        ) { sets, unit -> sets to unit }
            .onEach { (sets, unit) ->
                val unitChanged = currentUnit != unit
                currentUnit = unit
                _drafts.update { prev ->
                    sets.associate { set ->
                        val existing = prev[set.id]
                        val draft = when {
                            existing == null -> seedDraft(set, unit)
                            unitChanged -> existing.copy(load = loadText(set, unit))
                            else -> existing
                        }
                        set.id to draft
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun seedDraft(set: SetEntry, unit: WeightUnit) = StationDraft(
        // Time is empty-until-typed (targets carry no time); the rest prefill their actual-or-target.
        timeDigits = secondsToDigits(set.timeSec),
        reps = (set.reps ?: set.targetReps)?.toString().orEmpty(),
        load = loadText(set, unit),
        dist = (set.distanceM ?: set.targetDistanceM)?.toString().orEmpty(),
    )

    private fun loadText(set: SetEntry, unit: WeightUnit): String {
        val kg = set.loadKg ?: set.targetLoadKg ?: return ""
        val v = Units.toDisplay(kg, unit)
        return if (v % 1.0 == 0.0) v.toInt().toString() else ((v * 10).toInt() / 10.0).toString()
    }

    // Field edits (all station inputs are digit-only; time is a 6-digit clock buffer).
    fun onTimeChange(setId: String, digits: String) =
        editDraft(setId) { it.copy(timeDigits = digits.filter(Char::isDigit).takeLast(6)) }

    fun onRepsChange(setId: String, value: String) =
        editDraft(setId) { it.copy(reps = value.filter(Char::isDigit)) }

    fun onLoadChange(setId: String, value: String) =
        editDraft(setId) { it.copy(load = value.filter(Char::isDigit)) }

    fun onDistChange(setId: String, value: String) =
        editDraft(setId) { it.copy(dist = value.filter(Char::isDigit)) }

    private inline fun editDraft(setId: String, transform: (StationDraft) -> StationDraft) {
        _drafts.update { drafts -> drafts + (setId to transform(drafts[setId] ?: StationDraft())) }
    }

    fun addStation(segmentKey: String) {
        viewModelScope.launch {
            val (divisionKey, gender, raceMode) = preferencesRepository.getRaceInfo()
            val race = raceIdentity(divisionKey, gender, raceMode)
            repository.addStation(
                sessionId = sessionId,
                divisionKey = race.divisionKey,
                segmentKey = segmentKey,
                raceMode = race.raceMode,
                gender = race.gender,
            )
        }
    }

    fun addVariant(variant: HyroxVariant, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            val (divisionKey, gender, raceMode) = preferencesRepository.getRaceInfo()
            val race = raceIdentity(divisionKey, gender, raceMode)
            repository.addHyroxVariant(
                sessionId = sessionId,
                divisionKey = race.divisionKey,
                variant = variant,
                raceMode = race.raceMode,
                gender = race.gender,
                replaceExisting = replaceExisting,
            )
        }
    }

    fun confirmStation(setId: String) {
        val set = currentSets()[setId] ?: return
        val draft = _drafts.value[setId] ?: return
        viewModelScope.launch { repository.updateSet(sessionId, draftToSet(set, draft, currentUnit)) }
    }

    private fun draftToSet(set: SetEntry, draft: StationDraft, unit: WeightUnit): SetEntry {
        val useReps = set.targetReps != null
        val useLoad = set.targetReps == null && set.targetLoadKg != null
        val hasDist = set.targetDistanceM != null
        val secs = digitsToSeconds(draft.timeDigits)
        return set.copy(
            timeSec = secs.takeIf { it > 0 } ?: set.timeSec,
            reps = if (useReps) draft.reps.toIntOrNull() else set.reps,
            loadKg = if (useLoad) draft.load.toDoubleOrNull()?.let { Units.toKg(it, unit) } else set.loadKg,
            distanceM = if (hasDist) draft.dist.toIntOrNull() else set.distanceM,
        )
    }

    private fun currentSets(): Map<String, SetEntry> =
        uiState.value.sections.flatMap { it.items }.flatMap { it.sets }.associateBy { it.id }

    fun onNotesChange(notes: String) {
        lastNotes = notes
        notesInput.tryEmit(notes)
    }

    fun close(onDone: () -> Unit) {
        if (exiting) return
        exiting = true
        viewModelScope.launch {
            if (!repository.discardSessionIfEmpty(sessionId)) {
                lastNotes?.takeIf { it != uiState.value.notes }
                    ?.let { repository.updateSessionNotes(sessionId, it) }
            }
            onDone()
        }
    }

    fun removeEntry(entryId: String) {
        viewModelScope.launch { repository.removeEntry(sessionId, entryId) }
    }

    fun finish(onDone: () -> Unit) {
        if (exiting) return
        exiting = true
        viewModelScope.launch {
            val sets = currentSets()
            _drafts.value.forEach { (setId, draft) ->
                sets[setId]?.let { repository.updateSet(sessionId, draftToSet(it, draft, currentUnit)) }
            }
            repository.finishSession(sessionId, uiState.value.sessionType)
            onDone()
        }
    }
}

private fun HyroxStationModel.toStationOption(): StationOption =
    StationOption(segmentKey = segmentKey.orEmpty(), name = title, standard = value)

private data class RaceIdentity(val divisionKey: String, val gender: Gender, val raceMode: RaceMode)

private fun raceIdentity(divisionKey: String?, gender: Gender?, raceMode: RaceMode?): RaceIdentity {
    val resolvedGender =
        gender ?: if (divisionKey?.startsWith("WOMEN") == true) Gender.WOMEN else Gender.MEN
    return RaceIdentity(
        divisionKey = divisionKey ?: if (resolvedGender == Gender.WOMEN) "WOMEN" else "MEN",
        gender = resolvedGender,
        raceMode = raceMode ?: RaceMode.SINGLES,
    )
}
