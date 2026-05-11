package com.kartimer.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.kartimer.R
import com.kartimer.ui.theme.TimerGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val exportIntent by viewModel.exportShareIntent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // File picker for DB import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.importDatabase(context, uri)
            }
        }
    }

    // Handle export intent
    LaunchedEffect(exportIntent) {
        exportIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.clearExportIntent()
        }
    }

    LaunchedEffect(saveResult) {
        saveResult?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSaveResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_language),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ru" to "RU", "en" to "EN").forEach { (code, label) ->
                                val selected = currentLanguage == code
                                if (selected) {
                                    Button(
                                        onClick = {},
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                    ) { Text(label, fontWeight = FontWeight.Bold) }
                                } else {
                                    OutlinedButton(
                                        onClick = { onLanguageChange(code) },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                    ) { Text(label) }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_theme),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isDarkTheme) stringResource(R.string.theme_dark) else stringResource(R.string.theme_light),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onThemeChange
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.section_race_params),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                SettingsTextField(
                    value = formState.raceDurationMin,
                    onValueChange = { viewModel.updateRaceDuration(it) },
                    label = stringResource(R.string.field_race_duration),
                    supportingText = stringResource(R.string.field_race_duration_hint)
                )
            }

            item {
                SettingsTextField(
                    value = formState.maxSessionMin,
                    onValueChange = { viewModel.updateMaxSession(it) },
                    label = stringResource(R.string.field_max_session),
                    supportingText = stringResource(R.string.field_max_session_hint)
                )
            }

            item {
                SettingsTextField(
                    value = formState.minPilotTimeMin,
                    onValueChange = { viewModel.updateMinPilotTime(it) },
                    label = stringResource(R.string.field_min_pilot_time),
                    supportingText = stringResource(R.string.field_min_pilot_time_hint)
                )
            }

            item {
                SettingsTextField(
                    value = formState.minSessions,
                    onValueChange = { viewModel.updateMinSessions(it) },
                    label = stringResource(R.string.field_min_sessions),
                    supportingText = stringResource(R.string.field_min_sessions_hint)
                )
            }

            item {
                SettingsTextField(
                    value = formState.pitStopDurationSec,
                    onValueChange = { viewModel.updatePitStopDuration(it) },
                    label = stringResource(R.string.field_pit_stop_duration),
                    supportingText = stringResource(R.string.field_pit_stop_duration_hint)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_pit_stop_counted),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (formState.pitStopToNextSession)
                                    stringResource(R.string.pit_stop_to_next)
                                else
                                    stringResource(R.string.pit_stop_to_current),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = formState.pitStopToNextSession,
                            onCheckedChange = { viewModel.updatePitStopToNextSession(it) }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TimerGreen)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_save_settings), fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.section_database),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.label_export_db),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.desc_export_db),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.exportDatabase() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_export))
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.label_import_db),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.desc_import_db),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                                importLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_import))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = supportingText?.let { { Text(it) } }
    )
}
