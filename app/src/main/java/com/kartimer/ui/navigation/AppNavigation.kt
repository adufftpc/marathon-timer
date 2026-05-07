package com.kartimer.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kartimer.data.AppDatabase
import com.kartimer.data.repository.RaceRepository
import com.kartimer.ui.change.ChangePilotScreen
import com.kartimer.ui.change.ChangePilotViewModel
import com.kartimer.ui.change.ChangePilotViewModelFactory
import com.kartimer.ui.handicap.HandicapScreen
import com.kartimer.ui.handicap.HandicapViewModel
import com.kartimer.ui.handicap.HandicapViewModelFactory
import com.kartimer.ui.qr.QrCodeScreen
import com.kartimer.ui.race.RaceScreen
import com.kartimer.ui.race.RaceViewModel
import com.kartimer.ui.race.RaceViewModelFactory
import com.kartimer.ui.settings.SettingsScreen
import com.kartimer.ui.settings.SettingsViewModel
import com.kartimer.ui.settings.SettingsViewModelFactory
import com.kartimer.ui.history.SessionHistoryScreen
import com.kartimer.ui.history.SessionHistoryViewModel
import com.kartimer.ui.history.SessionHistoryViewModelFactory
import com.kartimer.ui.stats.StatsScreen
import com.kartimer.ui.stats.StatsViewModel
import com.kartimer.ui.stats.StatsViewModelFactory
import com.kartimer.ui.team.TeamSetupScreen
import com.kartimer.ui.team.TeamSetupViewModel
import com.kartimer.ui.team.TeamSetupViewModelFactory

object Routes {
    const val RACE = "race"
    const val CHANGE_PILOT = "change_pilot"
    const val HANDICAP = "handicap"
    const val QR_CODE = "qr_code"
    const val TEAM_SETUP = "team_setup"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val db = remember { AppDatabase.getInstance(appContext) }
    val repository = remember {
        RaceRepository(
            settingsDao = db.settingsDao(),
            teamDao = db.teamDao(),
            pilotDao = db.pilotDao(),
            sessionDao = db.sessionDao()
        )
    }

    // Shared RaceViewModel lives for the entire app session
    val raceViewModel: RaceViewModel = viewModel(
        factory = RaceViewModelFactory(repository)
    )

    var showQrDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.RACE
    ) {
        composable(Routes.RACE) {
            val historyViewModel: SessionHistoryViewModel = viewModel(
                factory = SessionHistoryViewModelFactory(repository)
            )
            val statsViewModel: StatsViewModel = viewModel(
                factory = StatsViewModelFactory(repository)
            )
            // Start on the Race page (index 1)
            val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> SessionHistoryScreen(viewModel = historyViewModel)
                        1 -> RaceScreen(
                            viewModel = raceViewModel,
                            onChangeClick = { navController.navigate(Routes.CHANGE_PILOT) },
                            onQrCodeClick = { showQrDialog = true },
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                            onTeamSetupClick = { navController.navigate(Routes.TEAM_SETUP) }
                        )
                        else -> StatsScreen(viewModel = statsViewModel)
                    }
                }

                // Page indicator dots (3 pages)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (pagerState.currentPage == index) 8.dp else 5.dp)
                                .background(
                                    color = if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        composable(Routes.CHANGE_PILOT) {
            val changePilotViewModel: ChangePilotViewModel = viewModel(
                factory = ChangePilotViewModelFactory(repository)
            )
            ChangePilotScreen(
                viewModel = changePilotViewModel,
                raceViewModel = raceViewModel,
                onConfirm = {
                    navController.popBackStack(Routes.RACE, inclusive = false)
                },
                onHandicapClick = { navController.navigate(Routes.HANDICAP) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HANDICAP) {
            val handicapViewModel: HandicapViewModel = viewModel(
                factory = HandicapViewModelFactory(repository)
            )
            HandicapScreen(
                viewModel = handicapViewModel,
                onFinished = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TEAM_SETUP) {
            val teamSetupViewModel: TeamSetupViewModel = viewModel(
                factory = TeamSetupViewModelFactory(repository)
            )
            TeamSetupScreen(
                viewModel = teamSetupViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(repository, appContext)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                onBack = { navController.popBackStack() }
            )
        }
    }

    // QR Code overlay dialog
    if (showQrDialog) {
        QrCodeScreen(
            raceViewModel = raceViewModel,
            onDismiss = { showQrDialog = false }
        )
    }
}
