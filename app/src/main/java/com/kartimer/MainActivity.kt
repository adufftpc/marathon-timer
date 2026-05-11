package com.kartimer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kartimer.ui.navigation.AppNavigation
import com.kartimer.ui.theme.MarathonTimerTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapWithLocale(readLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean(KEY_DARK_THEME, true)) }
            val currentLanguage = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

            MarathonTimerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { dark ->
                            isDarkTheme = dark
                            prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
                        },
                        currentLanguage = currentLanguage,
                        onLanguageChange = { lang ->
                            prefs.edit().putString(KEY_LANGUAGE, lang).apply()
                            updateApplicationLocale(applicationContext, lang)
                            recreate()
                        }
                    )
                }
            }
        }
    }
}
