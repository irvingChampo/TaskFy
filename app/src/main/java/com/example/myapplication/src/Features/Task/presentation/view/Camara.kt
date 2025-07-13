package com.example.myapplication.src.Features.Task.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.src.Core.Hardware.domain.CamaraRepository
import java.io.File

@Composable
fun Camara(
    cameraRepository: CamaraRepository,
    onPhotoCaptured: (File) -> Unit,
    onClose: () -> Unit
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

    var showError by remember { mutableStateOf<String?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showError = "Permiso de cámara requerido"
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Preview de la cámara
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        cameraRepository.initializeCamera(this, lifecycleOwner)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con controles
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón cerrar (arriba)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                // Controles de cámara (abajo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cambiar cámara
                    Button(
                        onClick = { cameraRepository.switchCamera() },
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "🔄",
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }

                    // Capturar foto
                    Button(
                        onClick = {
                            if (!isCapturing) {
                                isCapturing = true
                                val outputFile = File(
                                    context.externalMediaDirs.firstOrNull(),
                                    "photo_${System.currentTimeMillis()}.jpg"
                                )

                                cameraRepository.capturePhoto(
                                    outputFile = outputFile,
                                    onSuccess = { file ->
                                        isCapturing = false
                                        onPhotoCaptured(file)
                                    },
                                    onError = { exception ->
                                        isCapturing = false
                                        showError = "Error al capturar foto: ${exception.message}"
                                    }
                                )
                            }
                        },
                        enabled = !isCapturing,
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCapturing) Color.Gray else Color.White
                        )
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black
                            )
                        } else {
                            Text(
                                text = "📷",
                                fontSize = 32.sp,
                                color = Color.Black
                            )
                        }
                    }

                    // Espaciador para balance visual
                    Spacer(modifier = Modifier.size(60.dp))
                }
            }
        } else {
            // Sin permiso de cámara
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📷",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Permiso de cámara requerido",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                ) {
                    Text("Permitir acceso a cámara")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClose) {
                    Text("Cancelar")
                }
            }
        }

        showError?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                showError = null
            }

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red)
            ) {
                Text(
                    text = error,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}