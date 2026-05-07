package com.kartimer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "race_duration_min")
    val raceDurationMin: Int = 360,

    @ColumnInfo(name = "max_session_min")
    val maxSessionMin: Int = 35,

    @ColumnInfo(name = "min_pilot_time_min")
    val minPilotTimeMin: Int = 50,

    @ColumnInfo(name = "min_sessions")
    val minSessions: Int = 15,

    @ColumnInfo(name = "pit_stop_duration_sec")
    val pitStopDurationSec: Int = 75,

    @ColumnInfo(name = "pit_stop_to_next_session")
    val pitStopToNextSession: Boolean = false
)
