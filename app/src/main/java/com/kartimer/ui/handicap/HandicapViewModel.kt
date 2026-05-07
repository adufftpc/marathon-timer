package com.kartimer.ui.handicap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.data.repository.RaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HandicapViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _handicapSeconds = MutableStateFlow(0L)
    val handicapSeconds: StateFlow<Long> = _handicapSeconds.asStateFlow()

    private val _countdown = MutableStateFlow(0L)
    val countdown: StateFlow<Long> = _countdown.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _teamName = MutableStateFlow("")
    val teamName: StateFlow<String> = _teamName.asStateFlow()

    private var countdownJob: Job? = null
    private var startTime: Long = 0L

    init {
        loadHandicap()
    }

    private fun loadHandicap() {
        viewModelScope.launch {
            val teams = repository.getAllTeamsOnce()
            // Use the first team for simplicity; in a real app you'd pass the team ID
            val team = teams.firstOrNull()
            val handicap = team?.handicapSeconds?.toLong() ?: 0L
            _handicapSeconds.value = handicap
            _countdown.value = handicap
            _teamName.value = team?.name ?: "Команда"
            if (handicap > 0) {
                startCountdown(handicap)
            } else {
                _isFinished.value = true
            }
        }
    }

    private fun startCountdown(durationSec: Long) {
        startTime = System.currentTimeMillis()
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - startTime) / 1000L
                val remaining = maxOf(0L, durationSec - elapsed)
                _countdown.value = remaining
                if (remaining <= 0L) {
                    _isFinished.value = true
                    break
                }
            }
        }
    }

    fun cancel() {
        countdownJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

class HandicapViewModelFactory(
    private val repository: RaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HandicapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HandicapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
