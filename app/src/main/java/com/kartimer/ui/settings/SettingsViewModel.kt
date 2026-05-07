package com.kartimer.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kartimer.data.AppDatabase
import com.kartimer.data.entity.SettingsEntity
import com.kartimer.data.repository.RaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SettingsFormState(
    val raceDurationMin: String = "360",
    val maxSessionMin: String = "35",
    val minPilotTimeMin: String = "50",
    val minSessions: String = "15",
    val pitStopDurationSec: String = "75",
    val pitStopToNextSession: Boolean = false
)

class SettingsViewModel(
    private val repository: RaceRepository,
    private val appContext: Context
) : ViewModel() {

    private val _formState = MutableStateFlow(SettingsFormState())
    val formState: StateFlow<SettingsFormState> = _formState.asStateFlow()

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult.asStateFlow()

    private val _exportShareIntent = MutableStateFlow<Intent?>(null)
    val exportShareIntent: StateFlow<Intent?> = _exportShareIntent.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings().collect { entity ->
                val s = entity ?: SettingsEntity()
                _formState.value = SettingsFormState(
                    raceDurationMin = s.raceDurationMin.toString(),
                    maxSessionMin = s.maxSessionMin.toString(),
                    minPilotTimeMin = s.minPilotTimeMin.toString(),
                    minSessions = s.minSessions.toString(),
                    pitStopDurationSec = s.pitStopDurationSec.toString(),
                    pitStopToNextSession = s.pitStopToNextSession
                )
            }
        }
    }

    fun updateRaceDuration(v: String) {
        _formState.value = _formState.value.copy(raceDurationMin = v)
    }

    fun updateMaxSession(v: String) {
        _formState.value = _formState.value.copy(maxSessionMin = v)
    }

    fun updateMinPilotTime(v: String) {
        _formState.value = _formState.value.copy(minPilotTimeMin = v)
    }

    fun updateMinSessions(v: String) {
        _formState.value = _formState.value.copy(minSessions = v)
    }

    fun updatePitStopDuration(v: String) {
        _formState.value = _formState.value.copy(pitStopDurationSec = v)
    }

    fun updatePitStopToNextSession(v: Boolean) {
        _formState.value = _formState.value.copy(pitStopToNextSession = v)
    }

    fun saveSettings() {
        val form = _formState.value
        val raceDuration = form.raceDurationMin.toIntOrNull()
        val maxSession = form.maxSessionMin.toIntOrNull()
        val minPilotTime = form.minPilotTimeMin.toIntOrNull()
        val minSessions = form.minSessions.toIntOrNull()
        val pitStop = form.pitStopDurationSec.toIntOrNull()

        if (raceDuration == null || raceDuration <= 0) {
            _saveResult.value = "Введите корректную длительность гонки"
            return
        }
        if (maxSession == null || maxSession <= 0) {
            _saveResult.value = "Введите корректную макс. длительность сессии"
            return
        }
        if (minSessions == null || minSessions <= 0) {
            _saveResult.value = "Введите корректное минимальное количество сессий"
            return
        }
        if (pitStop == null || pitStop <= 0) {
            _saveResult.value = "Введите корректную длительность пит-стопа"
            return
        }

        viewModelScope.launch {
            repository.saveSettings(
                SettingsEntity(
                    id = 1,
                    raceDurationMin = raceDuration,
                    maxSessionMin = maxSession,
                    minPilotTimeMin = minPilotTime ?: 50,
                    minSessions = minSessions,
                    pitStopDurationSec = pitStop,
                    pitStopToNextSession = form.pitStopToNextSession
                )
            )
            _saveResult.value = "Настройки сохранены!"
        }
    }

    fun exportDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbFile = appContext.getDatabasePath(AppDatabase.DATABASE_NAME)
                if (!dbFile.exists()) {
                    _saveResult.value = "База данных не найдена"
                    return@launch
                }

                val exportDir = File(appContext.cacheDir, "exports").also { it.mkdirs() }
                val exportFile = File(exportDir, "marathon_timer_export.db")
                dbFile.copyTo(exportFile, overwrite = true)

                val uri: Uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    exportFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Marathon Timer Database Export")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    _exportShareIntent.value = Intent.createChooser(intent, "Экспорт базы данных")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _saveResult.value = "Ошибка экспорта: ${e.message}"
                }
            }
        }
    }

    fun importDatabase(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                AppDatabase.closeAndClear()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    _saveResult.value = "База данных импортирована. Перезапустите приложение."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _saveResult.value = "Ошибка импорта: ${e.message}"
                }
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    fun clearExportIntent() {
        _exportShareIntent.value = null
    }
}

class SettingsViewModelFactory(
    private val repository: RaceRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
