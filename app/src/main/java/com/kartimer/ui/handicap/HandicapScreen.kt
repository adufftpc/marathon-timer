package com.kartimer.ui.handicap

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.kartimer.ui.theme.TimerGreen
import com.kartimer.ui.theme.TimerOrange
import com.kartimer.ui.theme.TimerRed
import com.kartimer.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandicapScreen(
    viewModel: HandicapViewModel,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val countdown by viewModel.countdown.collectAsState()
    val handicapSeconds by viewModel.handicapSeconds.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val teamName by viewModel.teamName.collectAsState()

    // Auto-navigate back when finished
    LaunchedEffect(isFinished) {
        if (isFinished) {
            kotlinx.coroutines.delay(1500L)
            onFinished()
        }
    }

    val progressFraction = if (handicapSeconds > 0) countdown.toFloat() / handicapSeconds.toFloat() else 0f
    val countdownColor = when {
        isFinished -> TimerGreen
        countdown <= 5 -> TimerGreen
        countdown <= handicapSeconds / 2 -> TimerOrange
        else -> TimerRed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Гандикап") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancel()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Гандикап-старт",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(48.dp))

            if (isFinished) {
                Text(
                    text = "СТАРТ!",
                    fontSize = 72.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TimerGreen
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Гандикап истёк — команда выезжает!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TimerGreen
                )
            } else {
                Text(
                    text = TimeFormatter.formatMinSec(countdown),
                    fontSize = 80.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = countdownColor
                )
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = countdownColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Ожидайте старта...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Гандикап: ${handicapSeconds} секунд",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(48.dp))

            OutlinedButton(
                onClick = {
                    viewModel.cancel()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        }
    }
}
