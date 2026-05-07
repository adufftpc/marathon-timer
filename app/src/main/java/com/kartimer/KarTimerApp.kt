package com.kartimer

import android.app.Application
import com.kartimer.data.AppDatabase

class MarathonTimerApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Eagerly initialize the DB on first launch so Room creates the schema
        database
    }
}
