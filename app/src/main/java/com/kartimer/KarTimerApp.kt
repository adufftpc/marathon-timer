package com.kartimer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.kartimer.data.AppDatabase
import java.util.Locale

class MarathonTimerApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.wrapWithLocale(readLanguage(base)))
    }

    override fun onCreate() {
        super.onCreate()
        database
    }
}

const val PREFS_NAME = "marathon_timer_prefs"
const val KEY_DARK_THEME = "dark_theme"
const val KEY_LANGUAGE = "language"
const val DEFAULT_LANGUAGE = "ru"

fun readLanguage(context: Context): String =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

fun Context.wrapWithLocale(lang: String): Context {
    val locale = Locale(lang)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

@Suppress("DEPRECATION")
fun updateApplicationLocale(appContext: Context, lang: String) {
    val locale = Locale(lang)
    Locale.setDefault(locale)
    val config = appContext.resources.configuration
    config.setLocale(locale)
    appContext.resources.updateConfiguration(config, appContext.resources.displayMetrics)
}
