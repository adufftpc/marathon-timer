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
import androidx.compose.ui.res.stringResource
import com.kartimer.R
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
                    Text(if (isEditing) stringResource(R.string.team_edit_title) else stringResource(R.string.team_list_title))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) viewModel.cancelEdit() else onBack()
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (isEditing) stringResource(R.string.btn_cancel) else stringResource(R.string.btn_back)
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { viewModel.startNewTeam() }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_team))
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
                    text = stringResource(R.string.msg_no_teams),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddTeam) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_add_team))
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
                                text = stringResource(R.string.format_team_handicap, team.handicapSeconds),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = { onEditTeam(team) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit_team))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete_team), tint = TimerRed)
                        }
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text(stringResource(R.string.dialog_delete_team_title)) },
                        text = { Text(stringResource(R.string.format_dialog_delete_team_text, team.name)) },
                        confirmButton = {
                            TextButton(onClick = {
                                onDeleteTeam(team)
                                showDeleteDialog = false
                            }) {
                                Text(stringResource(R.string.btn_delete), color = TimerRed)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(R.string.btn_cancel))
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
                text = stringResource(R.string.section_team_data),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            OutlinedTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.field_team_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = formState.number,
                onValueChange = onNumberChange,
                label = { Text(stringResource(R.string.field_team_number)) },
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
                    label = { Text(stringResource(R.string.field_handicap_sec)) },
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
                    Text(stringResource(R.string.btn_calculate))
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
                    text = stringResource(R.string.section_pilots),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onAddPilot) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_add_pilot))
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
                            text = stringResource(R.string.format_pilot_number, index + 1),
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
                                    contentDescription = stringResource(R.string.cd_remove_pilot),
                                    tint = TimerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = pilot.name,
                        onValueChange = { onPilotNameChange(index, it) },
                        label = { Text(stringResource(R.string.field_pilot_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pilot.weight,
                        onValueChange = { onPilotWeightChange(index, it) },
                        label = { Text(stringResource(R.string.field_pilot_weight)) },
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
                    text = stringResource(R.string.btn_save_team),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
