package com.kartimer.ui.team

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.R
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.entity.TeamEntity
import com.kartimer.data.repository.RaceRepository
import com.kartimer.util.HandicapCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PilotFormState(
    val id: Int = 0,
    val name: String = "",
    val weight: String = ""
)

data class TeamFormState(
    val id: Int = 0,
    val name: String = "",
    val number: String = "",
    val handicapSeconds: String = "0",
    val pilots: List<PilotFormState> = emptyList()
)

class TeamSetupViewModel(
    private val repository: RaceRepository,
    private val appContext: Context
) : ViewModel() {

    private val _teams = MutableStateFlow<List<TeamEntity>>(emptyList())
    val teams: StateFlow<List<TeamEntity>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow<TeamFormState?>(null)
    val selectedTeam: StateFlow<TeamFormState?> = _selectedTeam.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult.asStateFlow()

    init {
        loadTeams()
    }

    private fun loadTeams() {
        viewModelScope.launch {
            repository.getAllTeams().collect { list ->
                _teams.value = list
            }
        }
    }

    fun startNewTeam() {
        _selectedTeam.value = TeamFormState(
            pilots = listOf(PilotFormState())
        )
        _isEditing.value = true
    }

    fun selectTeamForEdit(team: TeamEntity) {
        viewModelScope.launch {
            val pilots = repository.getPilotsByTeamOnce(team.id)
            _selectedTeam.value = TeamFormState(
                id = team.id,
                name = team.name,
                number = team.number.toString(),
                handicapSeconds = team.handicapSeconds.toString(),
                pilots = pilots.map { p ->
                    PilotFormState(
                        id = p.id,
                        name = p.name,
                        weight = p.weight?.toString() ?: ""
                    )
                }.ifEmpty { listOf(PilotFormState()) }
            )
            _isEditing.value = true
        }
    }

    fun cancelEdit() {
        _selectedTeam.value = null
        _isEditing.value = false
    }

    fun updateTeamName(name: String) {
        _selectedTeam.value = _selectedTeam.value?.copy(name = name)
    }

    fun updateTeamNumber(number: String) {
        _selectedTeam.value = _selectedTeam.value?.copy(number = number)
    }

    fun updateHandicap(value: String) {
        _selectedTeam.value = _selectedTeam.value?.copy(handicapSeconds = value)
    }

    fun addPilot() {
        val current = _selectedTeam.value ?: return
        _selectedTeam.value = current.copy(
            pilots = current.pilots + PilotFormState()
        )
    }

    fun updatePilotName(index: Int, name: String) {
        val current = _selectedTeam.value ?: return
        val updated = current.pilots.toMutableList()
        if (index < updated.size) {
            updated[index] = updated[index].copy(name = name)
        }
        _selectedTeam.value = current.copy(pilots = updated)
    }

    fun updatePilotWeight(index: Int, weight: String) {
        val current = _selectedTeam.value ?: return
        val updated = current.pilots.toMutableList()
        if (index < updated.size) {
            updated[index] = updated[index].copy(weight = weight)
        }
        _selectedTeam.value = current.copy(pilots = updated)
    }

    fun removePilot(index: Int) {
        val current = _selectedTeam.value ?: return
        if (current.pilots.size <= 1) return
        val updated = current.pilots.toMutableList()
        updated.removeAt(index)
        _selectedTeam.value = current.copy(pilots = updated)
    }

    fun calculateHandicap() {
        val current = _selectedTeam.value ?: return
        val pilotEntities = current.pilots.map { pf ->
            PilotEntity(
                id = pf.id,
                teamId = current.id,
                name = pf.name,
                weight = pf.weight.toDoubleOrNull()
            )
        }
        val handicap = HandicapCalculator.calculateHandicap(pilotEntities)
        _selectedTeam.value = current.copy(handicapSeconds = handicap.toString())
    }

    fun saveTeam() {
        val form = _selectedTeam.value ?: return
        val teamNumber = form.number.toIntOrNull()
        val handicap = form.handicapSeconds.toIntOrNull() ?: 0

        if (form.name.isBlank()) {
            _saveResult.value = appContext.getString(R.string.error_team_name_empty)
            return
        }
        if (teamNumber == null || teamNumber <= 0) {
            _saveResult.value = appContext.getString(R.string.error_invalid_team_number)
            return
        }

        viewModelScope.launch {
            val team = TeamEntity(
                id = form.id,
                name = form.name.trim(),
                number = teamNumber,
                handicapSeconds = handicap
            )
            val teamId = if (form.id == 0) {
                repository.saveTeam(team).toInt()
            } else {
                repository.updateTeam(team)
                form.id
            }

            // Delete old pilots if editing
            if (form.id != 0) {
                val oldPilots = repository.getPilotsByTeamOnce(form.id)
                val newPilotIds = form.pilots.map { it.id }.toSet()
                oldPilots.filter { it.id !in newPilotIds }.forEach {
                    repository.deletePilot(it)
                }
            }

            // Save pilots
            form.pilots.forEach { pf ->
                if (pf.name.isNotBlank()) {
                    val pilot = PilotEntity(
                        id = pf.id,
                        teamId = teamId,
                        name = pf.name.trim(),
                        weight = pf.weight.toDoubleOrNull()
                    )
                    if (pf.id == 0) {
                        repository.savePilot(pilot)
                    } else {
                        repository.updatePilot(pilot)
                    }
                }
            }

            _saveResult.value = appContext.getString(R.string.msg_team_saved)
            _isEditing.value = false
            _selectedTeam.value = null
        }
    }

    fun deleteTeam(teamId: Int) {
        viewModelScope.launch {
            repository.deleteTeamById(teamId)
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}

class TeamSetupViewModelFactory(
    private val repository: RaceRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamSetupViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
