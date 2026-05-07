package com.kartimer.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.data.entity.SettingsEntity
import com.kartimer.data.repository.RaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PilotStatsItem(
    val pilotId: Int,
    val pilotName: String,
    val totalSeconds: Int
)

data class TeamStatsGroup(
    val teamId: Int,
    val teamName: String,
    val teamNumber: Int,
    val pilots: List<PilotStatsItem>,
    // (raceDurationSec / pilotCount) + 30 min — unique per team
    val maxPilotTimeSec: Int
)

class StatsViewModel(private val repository: RaceRepository) : ViewModel() {

    val minPilotTimeSec: StateFlow<Int> = repository.getSettings()
        .map { (it ?: SettingsEntity()).minPilotTimeMin * 60 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 50 * 60)

    val teamStats: StateFlow<List<TeamStatsGroup>> = combine(
        repository.getAllPilots(),
        repository.getAllTeams(),
        repository.getAllSessions(),
        repository.getSettings()
    ) { pilots, teams, sessions, settings ->
        val s = settings ?: SettingsEntity()
        val raceDurationSec = s.raceDurationMin * 60

        val secondsByPilot = sessions
            .groupBy { it.pilotId }
            .mapValues { (_, s) -> s.sumOf { it.durationSeconds } }

        teams.sortedBy { it.number }.map { team ->
            val teamPilots = pilots.filter { it.teamId == team.id }
            val pilotCount = teamPilots.size.coerceAtLeast(1)
            val maxPilotTimeSec = raceDurationSec / pilotCount + 30 * 60

            TeamStatsGroup(
                teamId = team.id,
                teamName = team.name,
                teamNumber = team.number,
                maxPilotTimeSec = maxPilotTimeSec,
                pilots = teamPilots
                    .map { pilot ->
                        PilotStatsItem(
                            pilotId = pilot.id,
                            pilotName = pilot.name,
                            totalSeconds = secondsByPilot[pilot.id] ?: 0
                        )
                    }
                    .sortedByDescending { it.totalSeconds }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class StatsViewModelFactory(
    private val repository: RaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
