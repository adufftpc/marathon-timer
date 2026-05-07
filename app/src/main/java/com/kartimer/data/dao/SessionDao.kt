package com.kartimer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kartimer.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY session_number ASC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY session_number ASC")
    suspend fun getAllSessionsOnce(): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getSessionCountOnce(): Int

    @Query("SELECT * FROM sessions ORDER BY session_number DESC LIMIT 1")
    suspend fun getLastSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Query("UPDATE sessions SET best_lap_ms = :bestLapMs, avg_lap_ms = :avgLapMs WHERE id = :id")
    suspend fun updateLapTimes(id: Int, bestLapMs: Long?, avgLapMs: Long?)
}
