package com.kartimer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kartimer.data.dao.PilotDao
import com.kartimer.data.dao.SessionDao
import com.kartimer.data.dao.SettingsDao
import com.kartimer.data.dao.TeamDao
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.entity.SessionEntity
import com.kartimer.data.entity.SettingsEntity
import com.kartimer.data.entity.TeamEntity

@Database(
    entities = [
        SettingsEntity::class,
        TeamEntity::class,
        PilotEntity::class,
        SessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun settingsDao(): SettingsDao
    abstract fun teamDao(): TeamDao
    abstract fun pilotDao(): PilotDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "marathon_timer.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN pit_stop_to_next_session INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeAndClear() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
