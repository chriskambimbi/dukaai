package com.example.dukaai.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.dukaai.ui.theme.SlateBackground
import java.util.concurrent.Executors

/**
 * Camera Scanner Screen
 * Uses CameraX for camera preview and ML Kit for barcode scanning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(
    navController: NavController,
    onBarcodeScanned: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showPermissionDenied = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            camera?.let {
                                isFlashOn = !isFlashOn
                                it.cameraControl.enableTorch(isFlashOn)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (isFlashOn) "Flash On" else "Flash Off"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when {
            !hasCameraPermission && showPermissionDenied -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Camera permission required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Please grant camera permission to scan barcodes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
            hasCameraPermission -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CameraPreview(
                        onBarcodeDetected = { barcode ->
                            if (scannedBarcode == null) {
                                scannedBarcode = barcode
                                onBarcodeScanned(barcode)
                                navController.popBackStack()
                            }
                        },
                        onCameraCreated = { cam ->
                            camera = cam
                        }
                    )

                    // Scan area overlay
                    ScanOverlay()

                    // Instructions
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(32.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                text = "Position the barcode within the frame to scan",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    onCameraCreated: (Camera) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    val barcodeScanner = remember {
        BarcodeScanning.getClient()
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy, onBarcodeDetected)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                onCameraCreated(camera)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Use case binding failed", e)
            }

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            executor.shutdown()
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    imageProxy.image?.let { image ->
        val inputImage = InputImage.fromMediaImage(
            image,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_PRODUCT,
                        Barcode.TYPE_ISBN,
                        Barcode.TYPE_TEXT -> {
                            barcode.rawValue?.let { value ->
                                onBarcodeDetected(value)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeScanner", "Barcode scanning failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } ?: imageProxy.close()
}

@Composable
private fun ScanOverlay() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Semi-transparent background
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // Scan frame
        val frameWidth = canvasWidth * 0.7f
        val frameHeight = frameWidth * 0.6f
        val frameLeft = (canvasWidth - frameWidth) / 2
        val frameTop = (canvasHeight - frameHeight) / 2

        // Clear area for scan frame
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Scan frame border
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(16f),
            style = Stroke(
                width = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
            )
        )

        // Corner indicators
        val cornerLength = 40f
        val cornerWidth = 6f

        // Top-left corner
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft + cornerLength, frameTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft, frameTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // Top-right corner
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft + frameWidth, frameTop),
            end = Offset(frameLeft + frameWidth - cornerLength, frameTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft + frameWidth, frameTop),
            end = Offset(frameLeft + frameWidth, frameTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // Bottom-left corner
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft, frameTop + frameHeight),
            end = Offset(frameLeft + cornerLength, frameTop + frameHeight),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft, frameTop + frameHeight),
            end = Offset(frameLeft, frameTop + frameHeight - cornerLength),
            strokeWidth = cornerWidth
        )

        // Bottom-right corner
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft + frameWidth, frameTop + frameHeight),
            end = Offset(frameLeft + frameWidth - cornerLength, frameTop + frameHeight),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(frameLeft + frameWidth, frameTop + frameHeight),
            end = Offset(frameLeft + frameWidth, frameTop + frameHeight - cornerLength),
            strokeWidth = cornerWidth
        )
    }
}
