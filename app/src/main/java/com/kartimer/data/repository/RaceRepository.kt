package com.kartimer.data.repository

import com.kartimer.data.dao.PilotDao
import com.kartimer.data.dao.SessionDao
import com.kartimer.data.dao.SettingsDao
import com.kartimer.data.dao.TeamDao
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.entity.SessionEntity
import com.kartimer.data.entity.SettingsEntity
import com.kartimer.data.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

class RaceRepository(
    private val settingsDao: SettingsDao,
    private val teamDao: TeamDao,
    private val pilotDao: PilotDao,
    private val sessionDao: SessionDao
) {

    // ---- Settings ----

    fun getSettings(): Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun getSettingsOnce(): SettingsEntity {
        return settingsDao.getSettingsOnce() ?: SettingsEntity().also {
            settingsDao.insertSettings(it)
        }
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertSettings(settings)
    }

    // ---- Teams ----

    fun getAllTeams(): Flow<List<TeamEntity>> = teamDao.getAllTeams()

    suspend fun getAllTeamsOnce(): List<TeamEntity> = teamDao.getAllTeamsOnce()

    suspend fun getTeamById(id: Int): TeamEntity? = teamDao.getTeamById(id)

    suspend fun saveTeam(team: TeamEntity): Long = teamDao.insertTeam(team)

    suspend fun updateTeam(team: TeamEntity) = teamDao.updateTeam(team)

    suspend fun deleteTeam(team: TeamEntity) = teamDao.deleteTeam(team)

    suspend fun deleteTeamById(id: Int) = teamDao.deleteTeamById(id)

    // ---- Pilots ----

    fun getAllPilots(): Flow<List<PilotEntity>> = pilotDao.getAllPilots()

    suspend fun getAllPilotsOnce(): List<PilotEntity> = pilotDao.getAllPilotsOnce()

    fun getPilotsByTeam(teamId: Int): Flow<List<PilotEntity>> = pilotDao.getPilotsByTeam(teamId)

    suspend fun getPilotsByTeamOnce(teamId: Int): List<PilotEntity> =
        pilotDao.getPilotsByTeamOnce(teamId)

    suspend fun getPilotById(id: Int): PilotEntity? = pilotDao.getPilotById(id)

    suspend fun savePilot(pilot: PilotEntity): Long = pilotDao.insertPilot(pilot)

    suspend fun updatePilot(pilot: PilotEntity) = pilotDao.updatePilot(pilot)

    suspend fun deletePilot(pilot: PilotEntity) = pilotDao.deletePilot(pilot)

    suspend fun deletePilotById(id: Int) = pilotDao.deletePilotById(id)

    // ---- Sessions ----

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun getAllSessionsOnce(): List<SessionEntity> = sessionDao.getAllSessionsOnce()

    fun getSessionCount(): Flow<Int> = sessionDao.getSessionCount()

    suspend fun getSessionCountOnce(): Int = sessionDao.getSessionCountOnce()

    suspend fun getLastSession(): SessionEntity? = sessionDao.getLastSession()

    suspend fun saveSession(session: SessionEntity): Long = sessionDao.insertSession(session)

    suspend fun deleteAllSessions() = sessionDao.deleteAllSessions()

    suspend fun updateSessionLapTimes(id: Int, bestLapMs: Long?, avgLapMs: Long?) =
        sessionDao.updateLapTimes(id, bestLapMs, avgLapMs)
}
