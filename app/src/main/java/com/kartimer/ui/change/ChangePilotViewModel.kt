package com.kartimer.ui.change

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.repository.RaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePilotViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _pitStopCountdown = MutableStateFlow(0L)
    val pitStopCountdown: StateFlow<Long> = _pitStopCountdown.asStateFlow()

    private val _pilots = MutableStateFlow<List<PilotEntity>>(emptyList())
    val pilots: StateFlow<List<PilotEntity>> = _pilots.asStateFlow()

    private val _selectedPilot = MutableStateFlow<PilotEntity?>(null)
    val selectedPilot: StateFlow<PilotEntity?> = _selectedPilot.asStateFlow()

    private val _selectedKartNumber = MutableStateFlow(1)
    val selectedKartNumber: StateFlow<Int> = _selectedKartNumber.asStateFlow()

    private val _pitStopDurationSec = MutableStateFlow(75L)
    val pitStopDurationSec: StateFlow<Long> = _pitStopDurationSec.asStateFlow()

    private val _isPitStopFinished = MutableStateFlow(false)
    val isPitStopFinished: StateFlow<Boolean> = _isPitStopFinished.asStateFlow()

    private var countdownJob: Job? = null
    private var startTime: Long = 0L
    val pitStopStartTime: Long get() = startTime

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val settings = repository.getSettingsOnce()
            val duration = settings.pitStopDurationSec.toLong()
            _pitStopDurationSec.value = duration
            _pitStopCountdown.value = duration
            startPitStopCountdown(duration)
        }
        viewModelScope.launch {
            repository.getAllPilots().collect { list ->
                _pilots.value = list
            }
        }
    }

    private fun startPitStopCountdown(durationSec: Long) {
        startTime = System.currentTimeMillis()
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - startTime) / 1000L
                val remaining = maxOf(0L, durationSec - elapsed)
                _pitStopCountdown.value = remaining
                if (remaining <= 0L) {
                    _isPitStopFinished.value = true
                    break
                }
            }
        }
    }

    fun selectPilot(pilot: PilotEntity?) {
        _selectedPilot.value = pilot
    }

    fun selectKartNumber(kart: Int) {
        _selectedKartNumber.value = kart
    }

    fun canConfirm(): Boolean {
        return _selectedPilot.value != null && _selectedKartNumber.value in 1..99
    }

    fun stopCountdown() {
        countdownJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

class ChangePilotViewModelFactory(
    private val repository: RaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangePilotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChangePilotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
