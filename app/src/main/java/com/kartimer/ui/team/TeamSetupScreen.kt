package com.kartimer.ui.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.kartimer.data.entity.TeamEntity
import com.kartimer.ui.theme.TimerGreen
import com.kartimer.ui.theme.TimerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSetupScreen(
    viewModel: TeamSetupViewModel,
    onBack: () -> Unit
) {
    val teams by viewModel.teams.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveResult) {
        saveResult?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSaveResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Редактировать команду" else "Команды")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) viewModel.cancelEdit() else onBack()
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (isEditing) "Отмена" else "Назад"
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { viewModel.startNewTeam() }) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить команду")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isEditing && selectedTeam != null) {
            TeamEditForm(
                formState = selectedTeam!!,
                onNameChange = { viewModel.updateTeamName(it) },
                onNumberChange = { viewModel.updateTeamNumber(it) },
                onHandicapChange = { viewModel.updateHandicap(it) },
                onAddPilot = { viewModel.addPilot() },
                onPilotNameChange = { idx, name -> viewModel.updatePilotName(idx, name) },
                onPilotWeightChange = { idx, w -> viewModel.updatePilotWeight(idx, w) },
                onRemovePilot = { idx -> viewModel.removePilot(idx) },
                onCalculateHandicap = { viewModel.calculateHandicap() },
                onSave = { viewModel.saveTeam() },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            TeamListContent(
                teams = teams,
                onEditTeam = { viewModel.selectTeamForEdit(it) },
                onDeleteTeam = { viewModel.deleteTeam(it.id) },
                onAddTeam = { viewModel.startNewTeam() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun TeamListContent(
    teams: List<TeamEntity>,
    onEditTeam: (TeamEntity) -> Unit,
    onDeleteTeam: (TeamEntity) -> Unit,
    onAddTeam: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (teams.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Команды не добавлены",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddTeam) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить команду")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(teams) { team ->
                var showDeleteDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "№${team.number} ${team.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Гандикап: ${team.handicapSeconds} сек.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = { onEditTeam(team) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = TimerRed)
                        }
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Удалить команду") },
                        text = { Text("Удалить команду \"${team.name}\" и всех её пилотов?") },
                        confirmButton = {
                            TextButton(onClick = {
                                onDeleteTeam(team)
                                showDeleteDialog = false
                            }) {
                                Text("Удалить", color = TimerRed)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamEditForm(
    formState: TeamFormState,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onHandicapChange: (String) -> Unit,
    onAddPilot: () -> Unit,
    onPilotNameChange: (Int, String) -> Unit,
    onPilotWeightChange: (Int, String) -> Unit,
    onRemovePilot: (Int) -> Unit,
    onCalculateHandicap: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Данные команды",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            OutlinedTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = { Text("Название команды") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = formState.number,
                onValueChange = onNumberChange,
                label = { Text("Номер команды") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.handicapSeconds,
                    onValueChange = onHandicapChange,
                    label = { Text("Гандикап (сек)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Button(
                    onClick = onCalculateHandicap,
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Расчёт")
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Пилоты",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onAddPilot) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить пилота")
                }
            }
        }

        itemsIndexed(formState.pilots) { index, pilot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Пилот ${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        if (formState.pilots.size > 1) {
                            IconButton(
                                onClick = { onRemovePilot(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Удалить пилота",
                                    tint = TimerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = pilot.name,
                        onValueChange = { onPilotNameChange(index, it) },
                        label = { Text("Имя пилота") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pilot.weight,
                        onValueChange = { onPilotWeightChange(index, it) },
                        label = { Text("Вес (кг, необяз.)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TimerGreen)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Сохранить команду",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
