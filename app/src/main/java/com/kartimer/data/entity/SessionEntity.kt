package com.kartimer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PilotEntity::class,
            parentColumns = ["id"],
            childColumns = ["pilot_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("pilot_id")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "session_number")
    val sessionNumber: Int,

    @ColumnInfo(name = "pilot_id")
    val pilotId: Int?,

    @ColumnInfo(name = "kart_number")
    val kartNumber: Int,

    @ColumnInfo(name = "start_timestamp")
    val startTimestamp: Long,

    @ColumnInfo(name = "end_timestamp")
    val endTimestamp: Long,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,

    @ColumnInfo(name = "best_lap_ms")
    val bestLapMs: Long? = null,

    @ColumnInfo(name = "avg_lap_ms")
    val avgLapMs: Long? = null
)
