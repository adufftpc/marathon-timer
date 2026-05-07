package com.kartimer.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.kartimer.data.entity.PilotEntity
import com.kartimer.data.entity.SessionEntity
import com.kartimer.ui.theme.TimerGreen
import com.kartimer.ui.theme.TimerOrange
import com.kartimer.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(viewModel: SessionHistoryViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val pilotsWithSessions by viewModel.pilotsWithSessions.collectAsState()
    val filterPilotId by viewModel.filterPilotId.collectAsState()

    var expandedSessionId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "История сессий",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )

        // Pilot filter
        PilotFilterRow(
            pilots = pilotsWithSessions,
            selectedPilotId = filterPilotId,
            onSelectPilot = { viewModel.setFilter(it) }
        )

        Spacer(Modifier.height(8.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сессий ещё нет.\nНачните гонку и выполните смену пилота.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = sessions, key = { it.session.id }) { item ->
                    SessionCard(
                        item = item,
                        isExpanded = expandedSessionId == item.session.id,
                        onToggleExpand = {
                            expandedSessionId =
                                if (expandedSessionId == item.session.id) null
                                else item.session.id
                        },
                        onSaveLaps = { bestMs, avgMs ->
                            viewModel.updateLapTimes(item.session.id, bestMs, avgMs)
                            expandedSessionId = null
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PilotFilterRow(
    pilots: List<PilotEntity>,
    selectedPilotId: Int?,
    onSelectPilot: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = pilots.firstOrNull { it.id == selectedPilotId }?.name ?: "Все пилоты"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (pilots.isNotEmpty()) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Фильтр по пилоту") },
            leadingIcon = {
                Icon(Icons.Default.FilterList, contentDescription = null)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Все пилоты") },
                onClick = {
                    onSelectPilot(null)
                    expanded = false
                }
            )
            pilots.forEach { pilot ->
                DropdownMenuItem(
                    text = { Text(pilot.name) },
                    onClick = {
                        onSelectPilot(pilot.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
    item: SessionDisplayItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSaveLaps: (bestMs: Long?, avgMs: Long?) -> Unit
) {
    val session = item.session

    // Initialise edit fields from current DB values whenever the card becomes expanded
    var bestLapInput by remember(isExpanded) {
        mutableStateOf(session.bestLapMs?.let { TimeFormatter.formatLapTime(it) } ?: "")
    }
    var avgLapInput by remember(isExpanded) {
        mutableStateOf(session.avgLapMs?.let { TimeFormatter.formatLapTime(it) } ?: "")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {

            // ── Header row: number + pilot + duration ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "#${session.sessionNumber}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.pilotName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = TimeFormatter.formatMinSec(session.durationSeconds.toLong()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TimerGreen
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Time range row ──
            Text(
                text = "${TimeFormatter.formatTimestamp(session.startTimestamp)}  →  " +
                        TimeFormatter.formatTimestamp(session.endTimestamp),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(4.dp))

            // ── Lap times row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LapTimeChip(
                    label = "Лучший",
                    value = session.bestLapMs?.let { TimeFormatter.formatLapTime(it) },
                    modifier = Modifier.weight(1f)
                )
                LapTimeChip(
                    label = "Средний",
                    value = session.avgLapMs?.let { TimeFormatter.formatLapTime(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Expanded edit section ──
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val focusManager = LocalFocusManager.current
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Введите время круга (М:СС.мс)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bestLapInput,
                            onValueChange = { bestLapInput = it },
                            label = { Text("Лучший круг") },
                            placeholder = { Text("0:00.000") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next
                            ),
                            isError = bestLapInput.isNotEmpty() &&
                                    TimeFormatter.parseLapTime(bestLapInput) == null
                        )
                        OutlinedTextField(
                            value = avgLapInput,
                            onValueChange = { avgLapInput = it },
                            label = { Text("Средний круг") },
                            placeholder = { Text("0:00.000") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            isError = avgLapInput.isNotEmpty() &&
                                    TimeFormatter.parseLapTime(avgLapInput) == null
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val bestMs = if (bestLapInput.isBlank()) null
                                        else TimeFormatter.parseLapTime(bestLapInput)
                            val avgMs  = if (avgLapInput.isBlank()) null
                                        else TimeFormatter.parseLapTime(avgLapInput)
                            onSaveLaps(bestMs, avgMs)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TimerOrange
                        )
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LapTimeChip(label: String, value: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Text(
            text = value ?: "—",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = if (value != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}
