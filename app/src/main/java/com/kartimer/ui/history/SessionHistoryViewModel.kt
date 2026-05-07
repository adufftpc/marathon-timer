package com.kartimer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.entity.SessionEntity
import com.kartimer.data.repository.RaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionDisplayItem(
    val session: SessionEntity,
    val pilotName: String
)

class SessionHistoryViewModel(private val repository: RaceRepository) : ViewModel() {

    private val _filterPilotId = MutableStateFlow<Int?>(null)
    val filterPilotId: StateFlow<Int?> = _filterPilotId.asStateFlow()

    // All pilots that appear in at least one session (for the filter list)
    val pilotsWithSessions: StateFlow<List<PilotEntity>> = combine(
        repository.getAllPilots(),
        repository.getAllSessions()
    ) { pilots, sessions ->
        val pilotIdsInSessions = sessions.mapNotNull { it.pilotId }.toSet()
        pilots.filter { it.id in pilotIdsInSessions }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions: StateFlow<List<SessionDisplayItem>> = combine(
        repository.getAllSessions(),
        repository.getAllPilots(),
        _filterPilotId
    ) { sessions, pilots, filterPilotId ->
        val pilotMap = pilots.associateBy { it.id }
        sessions
            .filter { filterPilotId == null || it.pilotId == filterPilotId }
            .map { session ->
                SessionDisplayItem(
                    session = session,
                    pilotName = pilotMap[session.pilotId]?.name ?: "—"
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(pilotId: Int?) {
        _filterPilotId.value = pilotId
    }

    fun updateLapTimes(sessionId: Int, bestLapMs: Long?, avgLapMs: Long?) {
        viewModelScope.launch {
            repository.updateSessionLapTimes(sessionId, bestLapMs, avgLapMs)
        }
    }
}

class SessionHistoryViewModelFactory(
    private val repository: RaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
