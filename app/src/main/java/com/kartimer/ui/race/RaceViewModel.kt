package com.kartimer.ui.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.entity.SessionEntity
import com.kartimer.data.entity.SettingsEntity
import com.kartimer.data.entity.TeamEntity
import com.kartimer.data.repository.RaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WarningState {
    NONE,
    YELLOW_SESSION,
    RED_SESSION,
    YELLOW_COUNT,
    RED_COUNT
}

enum class RaceState {
    IDLE,
    RUNNING,
    PAUSED
}

data class TimerState(
    val elapsedSeconds: Long = 0L,
    val totalSeconds: Long = 0L
) {
    val remainingSeconds: Long get() = maxOf(0L, totalSeconds - elapsedSeconds)
    val isFinished: Boolean get() = elapsedSeconds >= totalSeconds && totalSeconds > 0
}

class RaceViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    // --- Race state ---
    private val _raceState = MutableStateFlow(RaceState.IDLE)
    val raceState: StateFlow<RaceState> = _raceState.asStateFlow()

    private val _raceTimerState = MutableStateFlow(TimerState())
    val raceTimerState: StateFlow<TimerState> = _raceTimerState.asStateFlow()

    private val _sessionTimerState = MutableStateFlow(0L)
    val sessionTimerState: StateFlow<Long> = _sessionTimerState.asStateFlow()

    private val _warningState = MutableStateFlow(WarningState.NONE)
    val warningState: StateFlow<WarningState> = _warningState.asStateFlow()

    private val _completedSessions = MutableStateFlow(0)
    val completedSessions: StateFlow<Int> = _completedSessions.asStateFlow()

    private val _currentPilot = MutableStateFlow<PilotEntity?>(null)
    val currentPilot: StateFlow<PilotEntity?> = _currentPilot.asStateFlow()

    private val _currentKartNumber = MutableStateFlow(0)
    val currentKartNumber: StateFlow<Int> = _currentKartNumber.asStateFlow()

    private val _currentTeam = MutableStateFlow<TeamEntity?>(null)
    val currentTeam: StateFlow<TeamEntity?> = _currentTeam.asStateFlow()

    private val _lastChangeTimestamp = MutableStateFlow(0L)
    val lastChangeTimestamp: StateFlow<Long> = _lastChangeTimestamp.asStateFlow()

    private val _settings = MutableStateFlow(SettingsEntity())
    val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()

    // Timestamps for precision timing
    private var raceStartWallTime: Long = 0L
    private var raceElapsedAtPause: Long = 0L
    private var sessionStartWallTime: Long = 0L
    private var sessionElapsedAtPause: Long = 0L

    private var timerJob: Job? = null

    init {
        loadSettings()
        observeSessionCount()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings().collect { entity ->
                val s = entity ?: SettingsEntity()
                _settings.value = s
                if (_raceState.value == RaceState.IDLE) {
                    _raceTimerState.update { it.copy(totalSeconds = s.raceDurationMin * 60L) }
                }
            }
        }
    }

    private fun observeSessionCount() {
        viewModelScope.launch {
            repository.getSessionCount().collect { count ->
                _completedSessions.value = count
                updateWarningState()
            }
        }
    }

    fun startRace() {
        if (_raceState.value != RaceState.IDLE) return
        val settings = _settings.value
        raceStartWallTime = System.currentTimeMillis()
        sessionStartWallTime = raceStartWallTime
        raceElapsedAtPause = 0L
        sessionElapsedAtPause = 0L
        _raceTimerState.update {
            it.copy(
                elapsedSeconds = 0L,
                totalSeconds = settings.raceDurationMin * 60L
            )
        }
        _sessionTimerState.value = 0L
        _raceState.value = RaceState.RUNNING
        startTickJob()
    }

    fun pauseRace() {
        if (_raceState.value != RaceState.RUNNING) return
        raceElapsedAtPause = _raceTimerState.value.elapsedSeconds
        sessionElapsedAtPause = _sessionTimerState.value
        timerJob?.cancel()
        _raceState.value = RaceState.PAUSED
    }

    fun resumeRace() {
        if (_raceState.value != RaceState.PAUSED) return
        raceStartWallTime = System.currentTimeMillis() - (raceElapsedAtPause * 1000L)
        sessionStartWallTime = System.currentTimeMillis() - (sessionElapsedAtPause * 1000L)
        _raceState.value = RaceState.RUNNING
        startTickJob()
    }

    fun stopRace() {
        timerJob?.cancel()
        _raceState.value = RaceState.PAUSED
        raceElapsedAtPause = _raceTimerState.value.elapsedSeconds
        sessionElapsedAtPause = _sessionTimerState.value
    }

    fun resetRace() {
        timerJob?.cancel()
        _raceState.value = RaceState.IDLE
        val settings = _settings.value
        _raceTimerState.value = TimerState(
            elapsedSeconds = 0L,
            totalSeconds = settings.raceDurationMin * 60L
        )
        _sessionTimerState.value = 0L
        _currentPilot.value = null
        _currentKartNumber.value = 0
        _warningState.value = WarningState.NONE
        raceElapsedAtPause = 0L
        sessionElapsedAtPause = 0L
        viewModelScope.launch {
            repository.deleteAllSessions()
        }
    }

    private fun startTickJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val now = System.currentTimeMillis()
                val raceElapsed = (now - raceStartWallTime) / 1000L
                val sessionElapsed = (now - sessionStartWallTime) / 1000L

                val settings = _settings.value
                val total = settings.raceDurationMin * 60L

                _raceTimerState.update {
                    it.copy(elapsedSeconds = raceElapsed, totalSeconds = total)
                }
                _sessionTimerState.value = sessionElapsed

                if (raceElapsed >= total) {
                    _raceState.value = RaceState.PAUSED
                    timerJob?.cancel()
                }

                updateWarningState()
            }
        }
    }

    private fun updateWarningState() {
        val settings = _settings.value
        val sessionElapsed = _sessionTimerState.value
        val raceRemaining = _raceTimerState.value.remainingSeconds
        val sessions = _completedSessions.value

        val maxSessionSec = settings.maxSessionMin * 60L
        val yellowSessionThreshold = maxSessionSec - 10 * 60L
        val redSessionThreshold = maxSessionSec - 3 * 60L

        val warningYellowCountThreshold = 40 * 60L
        val warningRedCountThreshold = 20 * 60L
        val minSessions = settings.minSessions

        val newWarning = when {
            sessionElapsed >= redSessionThreshold -> WarningState.RED_SESSION
            sessionElapsed >= yellowSessionThreshold -> WarningState.YELLOW_SESSION
            raceRemaining <= warningRedCountThreshold && sessions < minSessions -> WarningState.RED_COUNT
            raceRemaining <= warningYellowCountThreshold && sessions < minSessions -> WarningState.YELLOW_COUNT
            else -> WarningState.NONE
        }

        _warningState.value = newWarning
    }

    /**
     * Called when ChangePilotScreen confirms a pilot change.
     * Saves the completed session and resets the session timer.
     */
    fun onPilotChanged(newPilot: PilotEntity, newKartNumber: Int, pitStopStartTime: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val settings = _settings.value
            val toNext = settings.pitStopToNextSession
            val pitStopSec = settings.pitStopDurationSec.toLong()

            // Driving time = from last session start to when Change was pressed
            val drivingTimeSec = ((pitStopStartTime - sessionStartWallTime) / 1000L)
                .coerceAtLeast(0L)

            val sessionDuration: Int
            val sessionEndTimestamp: Long

            if (toNext) {
                // Pit stop counts toward NEXT session.
                // Outgoing session = pure driving time; pit stop not included.
                sessionDuration = drivingTimeSec.toInt()
                sessionEndTimestamp = pitStopStartTime
            } else {
                // Pit stop counts toward CURRENT (outgoing) session.
                // Always add exactly pitStopDurationSec, regardless of when OK was pressed.
                sessionDuration = (drivingTimeSec + pitStopSec).toInt()
                sessionEndTimestamp = pitStopStartTime + pitStopSec * 1000L
            }

            val isPreRace = _raceState.value == RaceState.IDLE
            val sessionCount = repository.getSessionCountOnce()

            if (!isPreRace && sessionDuration > 0) {
                repository.saveSession(
                    SessionEntity(
                        sessionNumber = sessionCount + 1,
                        pilotId = _currentPilot.value!!.id,
                        kartNumber = _currentKartNumber.value,
                        startTimestamp = sessionStartWallTime,
                        endTimestamp = sessionEndTimestamp,
                        durationSeconds = sessionDuration
                    )
                )
            }

            _currentPilot.value = newPilot
            _currentKartNumber.value = newKartNumber
            _currentTeam.value = repository.getTeamById(newPilot.teamId)
            _lastChangeTimestamp.value = now
            sessionElapsedAtPause = 0L

            if (toNext && !isPreRace) {
                // Mid-race change: new session carries the fixed pit stop duration already elapsed.
                sessionStartWallTime = now - pitStopSec * 1000L
                _sessionTimerState.value = pitStopSec
            } else {
                // Initial setup or "pit to current": new session starts fresh.
                sessionStartWallTime = now
                _sessionTimerState.value = 0L
            }
            updateWarningState()
        }
    }

    fun getCurrentTeamHandicap(): Int = 0 // resolved in HandicapViewModel
}

class RaceViewModelFactory(
    private val repository: RaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
