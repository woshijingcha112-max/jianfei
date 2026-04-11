package com.dietrecord.app.feature.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dietrecord.app.core.ui.components.AppAccentBadge
import com.dietrecord.app.core.ui.components.AppPageHeader
import com.dietrecord.app.core.ui.components.AppPrimaryButton
import com.dietrecord.app.core.ui.components.AppSecondaryButton
import com.dietrecord.app.core.ui.components.AppSectionCard
import com.dietrecord.app.core.ui.components.InlineFeedback
import com.dietrecord.app.core.ui.theme.BlossomPink
import com.dietrecord.app.core.ui.theme.CocoaBrown
import com.dietrecord.app.core.ui.theme.CreamWhite
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraScreen(
    uiState: CameraUiState,
    onRecognize: (File) -> Unit,
    onConsumeNavigation: () -> Unit,
    onToggleSimulateFailure: () -> Unit,
    onDismissError: () -> Unit,
    onCaptureError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
    }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by rememberSaveable { mutableStateOf(false) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            onCaptureError("Camera permission is required.")
        } else {
            onDismissError()
        }
    }

    val cameraPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(cameraPermissionGranted, permissionRequested) {
        if (!cameraPermissionGranted && !permissionRequested) {
            permissionRequested = true
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(cameraProviderFuture) {
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            cameraProvider = runCatching { cameraProviderFuture.get() }.getOrNull()
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    DisposableEffect(cameraProvider, cameraPermissionGranted, lifecycleOwner) {
        val provider = cameraProvider
        if (provider == null || !cameraPermissionGranted) {
            imageCapture = null
            onDispose { }
        } else {
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture
                )
            }.onSuccess {
                imageCapture = capture
                onDismissError()
            }.onFailure {
                imageCapture = null
                onCaptureError("Camera preview failed to start.")
            }
            onDispose {
                provider.unbindAll()
            }
        }
    }

    LaunchedEffect(uiState.navigateToResult) {
        if (uiState.navigateToResult) {
            onConsumeNavigation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppPageHeader(
            title = "拍照识别",
            subtitle = "对准餐食拍照，再上传到后端进行识别。",
            badgeText = "REAL"
        )

        AppSectionCard(
            eyebrow = "实时取景",
            title = "CameraX 预览 + 拍照 + 上传"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF8F5),
                                Color(0xFFFFE7DB)
                            )
                        )
                    )
            ) {
                if (cameraPermissionGranted) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x33000000)
                                    )
                                )
                            )
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppAccentBadge(
                            text = "权限未开启",
                            containerColor = CreamWhite.copy(alpha = 0.96f),
                            contentColor = BlossomPink
                        )
                        Text(
                            text = "请先授予相机权限，才能打开实时预览。",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = CocoaBrown,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(CreamWhite.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isCapturing) "拍照中" else "实时取景",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlossomPink
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppAccentBadge(
                        text = "上传到 /diet/photo/upload",
                        containerColor = CreamWhite.copy(alpha = 0.94f),
                        contentColor = CocoaBrown
                    )
                    Text(
                        text = "继续复用现有识别结果页与保存链路。",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CocoaBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AppSectionCard(
            eyebrow = "当前链路",
            title = "拍照、上传并复用现有识别会话状态"
        ) {
            Text(
                text = "拍照结果会先保存到应用内临时文件，再上传到后端识别接口。",
                style = MaterialTheme.typography.bodyLarge,
                color = CocoaBrown.copy(alpha = 0.74f)
            )
            if (uiState.errorMessage != null) {
                InlineFeedback(message = uiState.errorMessage, isError = true)
                AppSecondaryButton(
                    text = "清除提示",
                    onClick = onDismissError,
                    modifier = Modifier.fillMaxWidth(),
                    badgeText = "清"
                )
            }
        }

        AppPrimaryButton(
            text = when {
                uiState.isRecognizing -> "上传识别中..."
                isCapturing -> "拍照中..."
                else -> "拍照并识别"
            },
            onClick = {
                val currentCapture = imageCapture
                if (!cameraPermissionGranted) {
                    onCaptureError("请先授予相机权限。")
                    return@AppPrimaryButton
                }
                if (currentCapture == null) {
                    onCaptureError("相机仍在初始化，请稍后再试。")
                    return@AppPrimaryButton
                }
                if (uiState.isRecognizing || isCapturing) {
                    return@AppPrimaryButton
                }

                val photoFile = createCaptureFile(context)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                isCapturing = true
                currentCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            isCapturing = false
                            onRecognize(photoFile)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            isCapturing = false
                            onCaptureError("拍照失败，请重试。")
                        }
                    }
                )
            },
            enabled = !uiState.isRecognizing && !isCapturing,
            modifier = Modifier.fillMaxWidth(),
            supportingText = "本轮只替换示例餐图入口，不扩展其他识别交互。",
            badgeText = "拍"
        )

        AppSecondaryButton(
            text = if (uiState.simulateNextFailure) "已开启：下次识别失败" else "模拟下次识别失败",
            onClick = onToggleSimulateFailure,
            modifier = Modifier.fillMaxWidth(),
            supportingText = "只用于验证失败提示，不影响正式主链路。",
            badgeText = "测"
        )

        if (!cameraPermissionGranted) {
            AppSecondaryButton(
                text = "授予相机权限",
                onClick = {
                    permissionRequested = true
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = "授权后会自动拉起相机实时预览。",
                badgeText = "权"
            )
        }
    }
}

private fun createCaptureFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(context.cacheDir, "meal_$timeStamp.jpg")
}
