package com.dietrecord.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dietrecord.app.DietRecordApp
import com.dietrecord.app.core.ui.components.AppBackground
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite
import com.dietrecord.app.core.ui.theme.RibbonPink
import com.dietrecord.app.feature.camera.CameraScreen
import com.dietrecord.app.feature.camera.CameraViewModel
import com.dietrecord.app.feature.goal.GoalScreen
import com.dietrecord.app.feature.goal.GoalViewModel
import com.dietrecord.app.feature.home.HomeScreen
import com.dietrecord.app.feature.home.HomeViewModel
import com.dietrecord.app.feature.recognize.RecognizeResultScreen
import com.dietrecord.app.feature.recognize.RecognizeResultViewModel

/**
 * 应用导航宿主。
 *
 * 统一组装底部导航、页面路由和各页面 ViewModel。
 */
@Composable
fun AppNavHost() {
    val appContainer = (LocalContext.current.applicationContext as DietRecordApp).container
    val refreshVersion by appContainer.refreshCoordinator.mutationVersion.collectAsState()
    val homeViewModel: HomeViewModel = viewModel(factory = appContainer.homeViewModelFactory())
    val homeUiState by homeViewModel.uiState.collectAsState()
    val cameraViewModel: CameraViewModel = viewModel(factory = appContainer.cameraViewModelFactory())
    val cameraUiState by cameraViewModel.uiState.collectAsState()
    val goalViewModel: GoalViewModel = viewModel(factory = appContainer.goalViewModelFactory())
    val goalUiState by goalViewModel.uiState.collectAsState()
    val recognizeViewModel: RecognizeResultViewModel = viewModel(
        factory = appContainer.recognizeResultViewModelFactory()
    )
    val recognizeUiState by recognizeViewModel.uiState.collectAsState()

    var currentTopLevelRoute by rememberSaveable { mutableStateOf(AppDestination.Home.route) }
    var currentOverlayRoute by rememberSaveable { mutableStateOf<String?>(null) }
    var lastHandledHomeRefreshVersion by rememberSaveable { mutableLongStateOf(-1L) }
    var lastHandledGoalRefreshVersion by rememberSaveable { mutableLongStateOf(-1L) }
    var cameraCaptureRequestId by rememberSaveable { mutableStateOf(0) }
    var isCameraCapturePending by rememberSaveable { mutableStateOf(false) }

    fun navigateTopLevel(destination: AppDestination) {
        currentOverlayRoute = null
        currentTopLevelRoute = destination.route
        if (destination != AppDestination.Camera) {
            cameraCaptureRequestId = 0
            isCameraCapturePending = false
            cameraViewModel.clearError()
        }
    }

    fun openCameraFeature() {
        currentOverlayRoute = null
        cameraViewModel.clearError()
        cameraCaptureRequestId = 0
        currentTopLevelRoute = AppDestination.Camera.route
        isCameraCapturePending = false
    }

    LaunchedEffect(currentTopLevelRoute, currentOverlayRoute) {
        if (currentOverlayRoute != null) {
            return@LaunchedEffect
        }
        when (currentTopLevelRoute) {
            AppDestination.Home.route -> {
                homeViewModel.refresh()
                lastHandledHomeRefreshVersion = refreshVersion
            }

            AppDestination.Goal.route -> {
                goalViewModel.refresh()
                lastHandledGoalRefreshVersion = refreshVersion
            }
        }
    }

    LaunchedEffect(refreshVersion, currentTopLevelRoute, currentOverlayRoute) {
        if (currentOverlayRoute != null) {
            return@LaunchedEffect
        }
        when (currentTopLevelRoute) {
            AppDestination.Home.route -> {
                if (refreshVersion > lastHandledHomeRefreshVersion) {
                    homeViewModel.refresh()
                    lastHandledHomeRefreshVersion = refreshVersion
                }
            }

            AppDestination.Goal.route -> {
                if (refreshVersion > lastHandledGoalRefreshVersion) {
                    goalViewModel.refresh(preserveSuccessMessage = true)
                    lastHandledGoalRefreshVersion = refreshVersion
                }
            }
        }
    }

    LaunchedEffect(currentTopLevelRoute, isCameraCapturePending, cameraUiState.isRecognizing, cameraUiState.errorMessage) {
        val leftCameraFlow = currentTopLevelRoute != AppDestination.Camera.route
        if (leftCameraFlow || cameraUiState.isRecognizing || cameraUiState.errorMessage != null) {
            isCameraCapturePending = false
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentOverlayRoute != AppDestination.Recognize.route) {
                    val cameraSelected = currentTopLevelRoute == AppDestination.Camera.route
                    val homeSelected = currentTopLevelRoute == AppDestination.Home.route
                    val goalSelected = currentTopLevelRoute == AppDestination.Goal.route
                    val isCameraLoading = isCameraCapturePending || cameraUiState.isRecognizing

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CreamWhite.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        shadowElevation = 12.dp,
                        border = BorderStroke(1.dp, RibbonPink.copy(alpha = 0.28f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BottomNavTab(
                                destination = AppDestination.Home,
                                selected = homeSelected,
                                modifier = Modifier.weight(1f),
                                onClick = { navigateTopLevel(AppDestination.Home) }
                            )
                            CameraNavButton(
                                selected = cameraSelected,
                                isLoading = isCameraLoading,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (cameraSelected) {
                                        if (!isCameraLoading) {
                                            cameraViewModel.clearError()
                                            recognizeViewModel.prepareForNewRecognition()
                                            currentOverlayRoute = AppDestination.Recognize.route
                                            isCameraCapturePending = true
                                            cameraCaptureRequestId += 1
                                        }
                                    } else {
                                        openCameraFeature()
                                    }
                                }
                            )
                            BottomNavTab(
                                destination = AppDestination.Goal,
                                selected = goalSelected,
                                modifier = Modifier.weight(1f),
                                onClick = { navigateTopLevel(AppDestination.Goal) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentTopLevelRoute) {
                    AppDestination.Camera.route -> {
                        CameraScreen(
                            uiState = cameraUiState,
                            captureRequestId = cameraCaptureRequestId,
                            previewVisible = currentOverlayRoute != AppDestination.Recognize.route,
                            onRecognize = cameraViewModel::recognizeCapturedPhoto,
                            onDismissError = cameraViewModel::clearError,
                            onCaptureError = cameraViewModel::reportError
                        )
                    }

                    AppDestination.Goal.route -> {
                        GoalScreen(
                            uiState = goalUiState,
                            onCurrentWeightChange = goalViewModel::updateCurrentWeight,
                            onTargetWeightChange = goalViewModel::updateTargetWeight,
                            onDailyLimitChange = goalViewModel::updateDailyLimit,
                            onSave = goalViewModel::saveGoal,
                            onNavigateBack = {
                                navigateTopLevel(AppDestination.Home)
                            }
                        )
                    }

                    else -> {
                        HomeScreen(
                            uiState = homeUiState,
                            onOpenCamera = ::openCameraFeature,
                            onOpenGoal = { navigateTopLevel(AppDestination.Goal) }
                        )
                    }
                }

                if (currentOverlayRoute == AppDestination.Recognize.route) {
                    RecognizeResultScreen(
                        uiState = recognizeUiState,
                        isRecognitionPending = isCameraCapturePending || cameraUiState.isRecognizing,
                        recognitionErrorMessage = cameraUiState.errorMessage,
                        onSave = recognizeViewModel::saveRecord,
                        onRetake = {
                            recognizeViewModel.prepareForNewRecognition()
                            cameraViewModel.clearError()
                            cameraCaptureRequestId = 0
                            isCameraCapturePending = false
                            currentOverlayRoute = null
                            currentTopLevelRoute = AppDestination.Camera.route
                        },
                        onConsumeNavigation = {
                            recognizeViewModel.onNavigationHandled()
                            currentOverlayRoute = null
                            navigateTopLevel(AppDestination.Home)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    destination: AppDestination,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomNavIcon(destination = destination, selected = selected)
        Text(
            text = destination.label,
            modifier = Modifier.padding(top = 4.dp),
            color = if (selected) BlossomPink else CocoaBrown.copy(alpha = 0.52f)
        )
    }
}

@Composable
private fun CameraNavButton(
    selected: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomNavIcon(
            destination = AppDestination.Camera,
            selected = selected,
            isCameraLoading = isLoading
        )
        Text(
            text = AppDestination.Camera.label,
            modifier = Modifier.padding(top = 4.dp),
            color = if (selected || isLoading) BlossomPink else CocoaBrown.copy(alpha = 0.52f)
        )
    }
}

@Composable
private fun BottomNavIcon(
    destination: AppDestination,
    selected: Boolean,
    isCameraLoading: Boolean = false
) {
    if (destination == AppDestination.Camera) {
        val containerColor = if (selected || isCameraLoading) BlossomPink else CreamWhite
        val border = if (selected || isCameraLoading) null else BorderStroke(1.dp, RibbonPink.copy(alpha = 0.35f))
        val contentColor = if (selected || isCameraLoading) CreamWhite else CocoaBrown.copy(alpha = 0.62f)

        Surface(
            modifier = Modifier.size(52.dp),
            color = containerColor,
            contentColor = contentColor,
            shape = CircleShape,
            shadowElevation = if (selected || isCameraLoading) 10.dp else 4.dp,
            border = border
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCameraLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = contentColor,
                        trackColor = contentColor.copy(alpha = 0.22f),
                        strokeWidth = 2.2.dp
                    )
                } else {
                    Icon(
                        imageVector = destination.requireIcon(),
                        contentDescription = destination.label,
                        tint = contentColor
                    )
                }
            }
        }
    } else if (destination == AppDestination.Home && selected) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                val paw = BlossomPink.copy(alpha = 0.7f)
                drawCircle(color = paw, radius = 8.dp.toPx(), center = center.copy(y = center.y + 4.dp.toPx()))
                drawCircle(color = paw, radius = 4.dp.toPx(), center = center.copy(x = center.x - 10.dp.toPx(), y = center.y - 6.dp.toPx()))
                drawCircle(color = paw, radius = 4.dp.toPx(), center = center.copy(x = center.x - 3.dp.toPx(), y = center.y - 10.dp.toPx()))
                drawCircle(color = paw, radius = 4.dp.toPx(), center = center.copy(x = center.x + 5.dp.toPx(), y = center.y - 10.dp.toPx()))
                drawCircle(color = paw, radius = 4.dp.toPx(), center = center.copy(x = center.x + 12.dp.toPx(), y = center.y - 6.dp.toPx()))
            }
        }
    } else {
        Icon(
            imageVector = destination.requireIcon(),
            contentDescription = destination.label,
            tint = if (selected) BlossomPink else CocoaBrown.copy(alpha = 0.62f)
        )
    }
}
