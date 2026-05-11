package com.kartimer.ui.change

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import com.kartimer.R
import com.kartimer.ui.race.RaceViewModel
import com.kartimer.ui.theme.TimerGreen
import com.kartimer.ui.theme.TimerOrange
import com.kartimer.ui.theme.TimerRed
import com.kartimer.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePilotScreen(
    viewModel: ChangePilotViewModel,
    raceViewModel: RaceViewModel,
    onConfirm: () -> Unit,
    onHandicapClick: () -> Unit,
    onBack: () -> Unit
) {
    val countdown by viewModel.pitStopCountdown.collectAsState()
    val pitStopDuration by viewModel.pitStopDurationSec.collectAsState()
    val pilots by viewModel.pilots.collectAsState()
    val selectedPilot by viewModel.selectedPilot.collectAsState()
    val selectedKartNumber by viewModel.selectedKartNumber.collectAsState()
    val isPitStopFinished by viewModel.isPitStopFinished.collectAsState()

    var pilotDropdownExpanded by remember { mutableStateOf(false) }
    var kartNumberText by remember {
        mutableStateOf(if (selectedKartNumber > 0) selectedKartNumber.toString() else "")
    }

    val progressFraction = if (pitStopDuration > 0) countdown.toFloat() / pitStopDuration.toFloat() else 0f
    val countdownColor = when {
        countdown <= 10 -> TimerGreen
        countdown <= pitStopDuration / 2 -> TimerOrange
        else -> TimerRed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_pilot_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopCountdown()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pit stop countdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_pit_box),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = TimeFormatter.formatMinSec(countdown),
                        fontSize = 64.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = countdownColor
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progressFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = countdownColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                    if (isPitStopFinished) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.msg_pit_stop_done),
                            color = TimerGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pilot selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.label_select_pilot),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = pilotDropdownExpanded,
                        onExpandedChange = { pilotDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedPilot?.name ?: stringResource(R.string.placeholder_select_pilot),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = pilotDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = pilotDropdownExpanded,
                            onDismissRequest = { pilotDropdownExpanded = false }
                        ) {
                            if (pilots.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.msg_no_pilots)) },
                                    onClick = { pilotDropdownExpanded = false }
                                )
                            } else {
                                pilots.forEach { pilot ->
                                    DropdownMenuItem(
                                        text = { Text(pilot.name) },
                                        onClick = {
                                            viewModel.selectPilot(pilot)
                                            pilotDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Kart number input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.field_kart_no),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = kartNumberText,

                        onValueChange = { input ->
                            if (input.length <= 3 && input.all { it.isDigit() }) {
                                kartNumberText = input
                                val parsed = input.toIntOrNull() ?: 0
                                viewModel.selectKartNumber(parsed)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged( {focusState ->
                                if (focusState.isFocused) {
                                    kartNumberText = ""
                                    viewModel.selectKartNumber(0)
                                }
                            } ),
                        placeholder = { Text(stringResource(R.string.placeholder_kart_range)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = kartNumberText.isNotEmpty() &&
                                (kartNumberText.toIntOrNull() ?: 0) !in 1..999
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Confirm button
            val canConfirm = selectedPilot != null && selectedKartNumber in 1..999
            Button(
                onClick = {
                    val pilot = selectedPilot
                    if (pilot != null) {
                        viewModel.stopCountdown()
                        raceViewModel.onPilotChanged(pilot, selectedKartNumber, viewModel.pitStopStartTime)
                        onConfirm()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = TimerGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_confirm_change),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Handicap button
            Button(
                onClick = onHandicapClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) {
                Icon(Icons.Default.Timer, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_handicap),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
