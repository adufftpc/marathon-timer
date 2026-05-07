package com.kartimer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kartimer.data.entity.PilotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PilotDao {

    @Query("SELECT * FROM pilots ORDER BY name ASC")
    fun getAllPilots(): Flow<List<PilotEntity>>

    @Query("SELECT * FROM pilots ORDER BY name ASC")
    suspend fun getAllPilotsOnce(): List<PilotEntity>

    @Query("SELECT * FROM pilots WHERE team_id = :teamId ORDER BY name ASC")
    fun getPilotsByTeam(teamId: Int): Flow<List<PilotEntity>>

    @Query("SELECT * FROM pilots WHERE team_id = :teamId ORDER BY name ASC")
    suspend fun getPilotsByTeamOnce(teamId: Int): List<PilotEntity>

    @Query("SELECT * FROM pilots WHERE id = :id")
    suspend fun getPilotById(id: Int): PilotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPilot(pilot: PilotEntity): Long

    @Update
    suspend fun updatePilot(pilot: PilotEntity)

    @Delete
    suspend fun deletePilot(pilot: PilotEntity)

    @Query("DELETE FROM pilots WHERE id = :id")
    suspend fun deletePilotById(id: Int)
}
