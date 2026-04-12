package com.dietrecord.app.feature.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.view.View
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dietrecord.app.core.ui.components.AppSecondaryButton
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
    captureRequestId: Int,
    previewVisible: Boolean,
    onRecognize: (File) -> Unit,
    onDismissError: () -> Unit,
    onCaptureError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            setBackgroundColor(AndroidColor.BLACK)
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
    var handledCaptureRequestId by remember { mutableStateOf(0) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            onCaptureError("请先授予相机权限。")
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
                onCaptureError("相机预览启动失败，请重试。")
            }
            onDispose {
                provider.unbindAll()
            }
        }
    }

    fun triggerCapture() {
        val currentCapture = imageCapture
        if (!cameraPermissionGranted) {
            onCaptureError("请先授予相机权限。")
            return
        }
        if (currentCapture == null) {
            onCaptureError("相机正在启动，请稍后再试。")
            return
        }
        if (uiState.isRecognizing || isCapturing) {
            return
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
    }

    LaunchedEffect(captureRequestId, cameraPermissionGranted, imageCapture, uiState.isRecognizing) {
        if (captureRequestId == 0 || captureRequestId <= handledCaptureRequestId) {
            return@LaunchedEffect
        }
        if (!cameraPermissionGranted || imageCapture == null || uiState.isRecognizing || isCapturing) {
            return@LaunchedEffect
        }

        handledCaptureRequestId = captureRequestId
        triggerCapture()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black)
    ) {
        if (cameraPermissionGranted) {
            AndroidView(
                factory = { previewView },
                update = { view ->
                    view.visibility = if (previewVisible) View.VISIBLE else View.INVISIBLE
                    view.alpha = if (previewVisible) 1f else 0f
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "授予相机权限后即可拍照识别。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CreamWhite,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (previewVisible && uiState.errorMessage != null) {
            InlineFeedback(
                message = uiState.errorMessage,
                isError = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        if (!cameraPermissionGranted) {
            AppSecondaryButton(
                text = "授予相机权限",
                onClick = {
                    permissionRequested = true
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            )
        }

    }
}

private fun createCaptureFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(context.cacheDir, "meal_$timeStamp.jpg")
}
