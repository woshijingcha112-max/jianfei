package com.dietrecord.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dietrecord.app.DietRecordApp
import com.dietrecord.app.core.ui.components.AppBackground
import com.dietrecord.app.core.ui.components.AppRoundIconBadge
import com.dietrecord.app.feature.camera.CameraScreen
import com.dietrecord.app.feature.camera.CameraViewModel
import com.dietrecord.app.feature.goal.GoalScreen
import com.dietrecord.app.feature.goal.GoalViewModel
import com.dietrecord.app.feature.home.HomeScreen
import com.dietrecord.app.feature.home.HomeViewModel
import com.dietrecord.app.feature.recognize.RecognizeResultScreen
import com.dietrecord.app.feature.recognize.RecognizeResultViewModel
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite
import com.dietrecord.app.core.ui.theme.PetalPink
import com.dietrecord.app.core.ui.theme.RibbonPink

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val appContainer = (LocalContext.current.applicationContext as DietRecordApp).container
    val topLevelDestinations = listOf(
        AppDestination.Home,
        AppDestination.Camera,
        AppDestination.Goal
    )

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CreamWhite.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, RibbonPink.copy(alpha = 0.45f))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = CreamWhite,
                                    selectedTextColor = BlossomPink,
                                    unselectedIconColor = CocoaBrown.copy(alpha = 0.72f),
                                    unselectedTextColor = CocoaBrown.copy(alpha = 0.62f)
                                ),
                                icon = {
                                    AppRoundIconBadge(
                                        text = destination.label.take(1),
                                        containerColor = if (selected) BlossomPink else PetalPink,
                                        contentColor = if (selected) CreamWhite else CocoaBrown
                                    )
                                },
                                label = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppDestination.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(factory = appContainer.homeViewModelFactory())
                    val homeUiState by homeViewModel.uiState.collectAsState()
                    HomeScreen(
                        uiState = homeUiState,
                        onOpenCamera = { navController.navigate(AppDestination.Camera.route) },
                        onOpenGoal = { navController.navigate(AppDestination.Goal.route) }
                    )
                }
                composable(AppDestination.Camera.route) {
                    val cameraViewModel: CameraViewModel = viewModel(factory = appContainer.cameraViewModelFactory())
                    val cameraUiState by cameraViewModel.uiState.collectAsState()
                    CameraScreen(
                        uiState = cameraUiState,
                        onRecognize = cameraViewModel::recognizeCapturedPhoto,
                        onConsumeNavigation = {
                            cameraViewModel.onNavigationHandled()
                            navController.navigate(AppDestination.Recognize.route)
                        },
                        onToggleSimulateFailure = cameraViewModel::toggleSimulateNextFailure,
                        onDismissError = cameraViewModel::clearError,
                        onCaptureError = cameraViewModel::reportError
                    )
                }
                composable(AppDestination.Recognize.route) {
                    val recognizeViewModel: RecognizeResultViewModel = viewModel(
                        factory = appContainer.recognizeResultViewModelFactory()
                    )
                    val recognizeUiState by recognizeViewModel.uiState.collectAsState()
                    RecognizeResultScreen(
                        uiState = recognizeUiState,
                        onSave = recognizeViewModel::saveRecord,
                        onConsumeNavigation = {
                            recognizeViewModel.onNavigationHandled()
                            navController.navigate(AppDestination.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        onToggleSimulateFailure = recognizeViewModel::toggleSimulateNextFailure
                    )
                }
                composable(AppDestination.Goal.route) {
                    val goalViewModel: GoalViewModel = viewModel(factory = appContainer.goalViewModelFactory())
                    val goalUiState by goalViewModel.uiState.collectAsState()
                    GoalScreen(
                        uiState = goalUiState,
                        onCurrentWeightChange = goalViewModel::updateCurrentWeight,
                        onTargetWeightChange = goalViewModel::updateTargetWeight,
                        onDailyLimitChange = goalViewModel::updateDailyLimit,
                        onSave = goalViewModel::saveGoal,
                        onNavigateBack = {
                            val popped = navController.popBackStack()
                            if (!popped) {
                                navController.navigate(AppDestination.Home.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        onToggleSimulateFailure = goalViewModel::toggleSimulateNextFailure
                    )
                }
            }
        }
    }
}
