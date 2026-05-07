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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
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
        WarningState.YELLOW_SESSION -> "ВНИМАНИЕ: Время сессии истекает скоро!"
        WarningState.RED_SESSION -> "СРОЧНО: Немедленно сменить пилота!"
        WarningState.YELLOW_COUNT -> "ВНИМАНИЕ: Мало сессий, ускорьтесь!"
        WarningState.RED_COUNT -> "СРОЧНО: Критически мало сессий!"
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
                text = "Marathon Timer",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onTeamSetupClick) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Команды",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
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
                    text = "ГОНКА",
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
                            text = "Осталось",
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
                            text = "Сессий",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "$completedSessions / ${settings.minSessions}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (completedSessions < settings.minSessions &&
                                raceTimerState.remainingSeconds <= 40 * 60
                            ) WarningYellow else MaterialTheme.colorScheme.onSurface
                        )
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
                    text = "ТЕКУЩАЯ СЕССИЯ",
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
                            text = "Пилот",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = currentPilot?.name ?: "—",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Карт №",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (currentKartNumber > 0) currentKartNumber.toString() else "—",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Макс. сессия",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${settings.maxSessionMin} мин",
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
                Text("Смена", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onQrCodeClick,
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = currentPilot != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("QR", fontWeight = FontWeight.Bold)
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
                        Text("Старт", fontWeight = FontWeight.Bold)
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
                        Text("Пауза", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TimerRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Сброс", fontWeight = FontWeight.Bold)
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
                        Text("Продолжить", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TimerRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Сброс", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сброс гонки") },
            text = { Text("Сбросить таймер и все данные гонки? Это действие необратимо.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetRace()
                        showResetDialog = false
                    }
                ) {
                    Text("Сброс", color = TimerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
