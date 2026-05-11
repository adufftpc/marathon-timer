package com.kartimer.ui.race

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.kartimer.R
import com.kartimer.ui.theme.*
import com.kartimer.util.TimeFormatter

@Composable
fun RaceScreen(
    viewModel: RaceViewModel,
    onChangeClick: () -> Unit,
    onQrCodeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTeamSetupClick: () -> Unit
) {
    val raceState by viewModel.raceState.collectAsState()
    val raceTimerState by viewModel.raceTimerState.collectAsState()
    val sessionTimerState by viewModel.sessionTimerState.collectAsState()
    val warningState by viewModel.warningState.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()
    val currentPilot by viewModel.currentPilot.collectAsState()
    val currentKartNumber by viewModel.currentKartNumber.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    val warningBgColor by animateColorAsState(
        targetValue = when (warningState) {
            WarningState.YELLOW_SESSION, WarningState.YELLOW_COUNT -> WarningYellow
            WarningState.RED_SESSION, WarningState.RED_COUNT -> WarningRed
            WarningState.NONE -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 500),
        label = "warningColor"
    )

    val warningTextColor = when (warningState) {
        WarningState.YELLOW_SESSION, WarningState.YELLOW_COUNT -> WarningOnYellow
        WarningState.RED_SESSION, WarningState.RED_COUNT -> Color.White
        WarningState.NONE -> Color.Transparent
    }

    val warningText = when (warningState) {
        WarningState.YELLOW_SESSION -> stringResource(R.string.warning_yellow_session)
        WarningState.RED_SESSION -> stringResource(R.string.warning_red_session)
        WarningState.YELLOW_COUNT -> stringResource(R.string.warning_yellow_count)
        WarningState.RED_COUNT -> stringResource(R.string.warning_red_count)
        WarningState.NONE -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onTeamSetupClick) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = stringResource(R.string.cd_teams),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Warning banner
        if (warningState != WarningState.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(warningBgColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = warningText,
                    color = warningTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Race timer card
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
                    text = stringResource(R.string.label_race),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TimeFormatter.formatSeconds(raceTimerState.elapsedSeconds),
                    fontSize = 52.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        raceTimerState.remainingSeconds <= 20 * 60 -> TimerRed
                        raceTimerState.remainingSeconds <= 40 * 60 -> TimerOrange
                        else -> TimerGreen
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.label_remaining),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = TimeFormatter.formatSeconds(raceTimerState.remainingSeconds),
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.label_sessions),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.format_sessions_count, completedSessions, settings.minSessions),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (completedSessions < settings.minSessions &&
                                raceTimerState.remainingSeconds <= 40 * 60
                            ) WarningYellow else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                val restSessions = (settings.minSessions - completedSessions).coerceAtLeast(0)
                val avgSessionMin = if (restSessions > 0)
                    raceTimerState.remainingSeconds / 60L / restSessions
                else 0L

                if (raceTimerState.totalSeconds > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.label_rest_sessions),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = restSessions.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (restSessions > 0) MaterialTheme.colorScheme.onSurface
                                        else TimerGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.label_avg_per_session),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (restSessions > 0) stringResource(R.string.format_value_min, avgSessionMin) else stringResource(R.string.label_na),
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Session timer card
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
                    text = stringResource(R.string.label_current_session),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TimeFormatter.formatMinSec(sessionTimerState),
                    fontSize = 42.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when (warningState) {
                        WarningState.RED_SESSION -> WarningRed
                        WarningState.YELLOW_SESSION -> WarningYellow
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Current pilot & kart info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.label_pilot),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = currentPilot?.name ?: stringResource(R.string.label_na),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.label_kart_number),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (currentKartNumber > 0) currentKartNumber.toString() else stringResource(R.string.label_na),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.label_max_session),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.format_value_min, settings.maxSessionMin),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onChangeClick,
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = true,
                colors = ButtonDefaults.buttonColors(containerColor = MarathonTimerPrimary)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_change_pilot), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onQrCodeClick,
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = currentPilot != null,
                colors = ButtonDefaults.buttonColors(containerColor = QrButtonColor)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_qr), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Race control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (raceState) {
                RaceState.IDLE -> {
                    Button(
                        onClick = { viewModel.startRace() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = currentPilot != null,
                        colors = ButtonDefaults.buttonColors(containerColor = TimerGreen)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_start), fontWeight = FontWeight.Bold)
                    }
                }
                RaceState.RUNNING -> {
                    Button(
                        onClick = { viewModel.pauseRace() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TimerOrange)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_pause), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TimerRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_reset), fontWeight = FontWeight.Bold)
                    }
                }
                RaceState.PAUSED -> {
                    Button(
                        onClick = { viewModel.resumeRace() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TimerGreen)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_resume), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TimerRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_reset), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.dialog_reset_title)) },
            text = { Text(stringResource(R.string.dialog_reset_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetRace()
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_reset), color = TimerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
