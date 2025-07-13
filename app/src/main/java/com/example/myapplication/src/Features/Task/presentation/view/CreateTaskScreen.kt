package com.example.myapplication.src.Features.Task.presentation.view

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.src.Features.Task.presentation.viewmodel.TaskViewModel
import com.example.myapplication.src.Features.Task.presentation.viewmodel.TaskViewModelFactory
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onBackClick: () -> Unit = {},
    onTaskCreated: () -> Unit = {},
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory())
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("media") }
    var estado by remember { mutableStateOf("pendiente") }
    var capturedPhoto by remember { mutableStateOf<File?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val isLoading by taskViewModel.isLoading.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    val operationSuccess by taskViewModel.operationSuccess.collectAsState()

    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            taskViewModel.clearOperationSuccess()
            onTaskCreated()
        }
    }

    LaunchedEffect(titulo, descripcion) {
        if (errorMessage != null) {
            taskViewModel.clearError()
        }
    }

    if (showCamera) {
        TaskCameraScreen(
            taskViewModel = taskViewModel,
            lifecycleOwner = lifecycleOwner,
            onPhotoCaptured = { file ->
                capturedPhoto = file
                showCamera = false
            },
            onClose = {
                showCamera = false
            }
        )
    } else {
        CreateTaskContent(
            titulo = titulo,
            onTituloChange = { titulo = it },
            descripcion = descripcion,
            onDescripcionChange = { descripcion = it },
            fecha = fecha,
            onFechaChange = { fecha = it },
            prioridad = prioridad,
            onPrioridadChange = { prioridad = it },
            estado = estado,
            onEstadoChange = { estado = it },
            capturedPhoto = capturedPhoto,
            onCameraClick = { showCamera = true },
            onBackClick = onBackClick,
            onSaveClick = {
                if (titulo.isNotBlank()) {
                    taskViewModel.crearTarea(
                        titulo = titulo,
                        descripcion = descripcion.ifBlank { null },
                        prioridad = prioridad,
                        estado = estado,
                        fecha = fecha.ifBlank { null },
                        capturedPhoto = capturedPhoto
                    )
                }
            },
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }
}

@Composable
private fun CreateTaskContent(
    titulo: String,
    onTituloChange: (String) -> Unit,
    descripcion: String,
    onDescripcionChange: (String) -> Unit,
    fecha: String,
    onFechaChange: (String) -> Unit,
    prioridad: String,
    onPrioridadChange: (String) -> Unit,
    estado: String,
    onEstadoChange: (String) -> Unit,
    capturedPhoto: File?,
    onCameraClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CreateTaskHeader(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TituloField(
                    value = titulo,
                    onValueChange = onTituloChange
                )

                DescripcionField(
                    value = descripcion,
                    onValueChange = onDescripcionChange
                )

                FechaFieldWithDatePicker(
                    value = fecha,
                    onValueChange = onFechaChange
                )

                PrioridadField(
                    value = prioridad,
                    onValueChange = onPrioridadChange
                )

                EstadoField(
                    value = estado,
                    onValueChange = onEstadoChange
                )

                FotoSection(
                    capturedPhoto = capturedPhoto,
                    onCameraClick = onCameraClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                SaveButton(
                    onClick = onSaveClick,
                    enabled = !isLoading && titulo.isNotBlank(),
                    isLoading = isLoading
                )

                errorMessage?.let { error ->
                    ErrorCard(error = error)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun CreateTaskHeader(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF2196F3),
                        Color(0xFF42A5F5)
                    )
                )
            )
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = "Agregar tarea",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea una nueva tarea",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun TituloField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📝 Título",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "Título:",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color(0xFF2196F3),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
private fun DescripcionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📄 Descripción",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "Descripción:",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color(0xFF2196F3),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )
    }
}

@Composable
private fun FechaFieldWithDatePicker(
    value: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📅 Fecha",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (value.isNotEmpty()) Color(0xFF2196F3) else Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (value.isNotEmpty()) value else "Seleccionar fecha",
                        color = if (value.isNotEmpty()) Color.Black else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = if (value.isNotEmpty()) FontWeight.Medium else FontWeight.Normal
                    )
                    if (value.isNotEmpty()) {
                        Text(
                            text = "Toca para cambiar",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "DD/MM/AAAA",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendario",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()

        if (value.isNotEmpty()) {
            try {
                val parts = value.split("/")
                if (parts.size == 3) {
                    calendar.set(
                        parts[2].toInt(),
                        parts[1].toInt() - 1,
                        parts[0].toInt()
                    )
                }
            } catch (e: Exception) {
                // Usar fecha actual si hay error
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
                onValueChange(formattedDate)
                showDatePicker = false
            },
            year,
            month,
            day
        ).also { dialog ->
            dialog.setOnDismissListener { showDatePicker = false }
            dialog.show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioridadField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val prioridades = listOf(
        "alta" to "🔴 Alta",
        "media" to "🟡 Media",
        "baja" to "🟢 Baja"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⚡ Prioridad",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { expanded = true },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (expanded) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val selectedPrioridad = prioridades.find { it.first == value }
                        Text(
                            text = selectedPrioridad?.second ?: "Seleccionar prioridad",
                            color = if (selectedPrioridad != null) Color.Black else Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = if (selectedPrioridad != null) FontWeight.Medium else FontWeight.Normal
                        )
                        Text(
                            text = "Nivel de importancia",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                prioridades.forEach { (prioridadValue, prioridadLabel) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = prioridadLabel,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                if (prioridadValue == value) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onValueChange(prioridadValue)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (prioridadValue == value) Color(0xFFF0F8FF) else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadoField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val estados = listOf(
        "pendiente" to "⏳ Pendiente",
        "completada" to "✅ Completada"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📋 Estado",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { expanded = true },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (expanded) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val selectedEstado = estados.find { it.first == value }
                        Text(
                            text = selectedEstado?.second ?: "Seleccionar estado",
                            color = if (selectedEstado != null) Color.Black else Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = if (selectedEstado != null) FontWeight.Medium else FontWeight.Normal
                        )
                        Text(
                            text = "Estado de la tarea",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                estados.forEach { (estadoValue, estadoLabel) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = estadoLabel,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                if (estadoValue == value) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onValueChange(estadoValue)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (estadoValue == value) Color(0xFFF0F8FF) else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FotoSection(
    capturedPhoto: File?,
    onCameraClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📷 Foto (Opcional)",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onCameraClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (capturedPhoto != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(capturedPhoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📸",
                                fontSize = 24.sp
                            )
                            Text(
                                text = "Foto capturada",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Toca para cambiar",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "Toca para tomar foto",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💾",
                    fontSize = 18.sp
                )
                Text(
                    text = "Guardar Tarea",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = error,
            color = Color.Red,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun TaskCameraScreen(
    taskViewModel: TaskViewModel,
    lifecycleOwner: LifecycleOwner,
    onPhotoCaptured: (File) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val cameraRepository = taskViewModel.getCameraRepository()

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
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        cameraRepository.initializeCamera(this, lifecycleOwner)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            TaskCameraControls(
                cameraRepository = cameraRepository,
                onClose = onClose,
                onPhotoCaptured = onPhotoCaptured,
                isCapturing = isCapturing,
                onCapturingChange = { isCapturing = it },
                onError = { showError = it }
            )
        } else {
            TaskCameraPermissionScreen(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onClose = onClose
            )
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

@Composable
private fun TaskCameraControls(
    cameraRepository: com.example.myapplication.src.Core.Hardware.domain.CamaraRepository,
    onClose: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    isCapturing: Boolean,
    onCapturingChange: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Button(
                onClick = {
                    if (!isCapturing) {
                        onCapturingChange(true)
                        val outputFile = File(
                            context.externalMediaDirs.firstOrNull(),
                            "photo_${System.currentTimeMillis()}.jpg"
                        )

                        cameraRepository.capturePhoto(
                            outputFile = outputFile,
                            onSuccess = { file ->
                                onCapturingChange(false)
                                onPhotoCaptured(file)
                            },
                            onError = { exception ->
                                onCapturingChange(false)
                                onError("Error al capturar foto: ${exception.message}")
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

            Spacer(modifier = Modifier.size(60.dp))
        }
    }
}

@Composable
private fun TaskCameraPermissionScreen(
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
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
            onClick = onRequestPermission
        ) {
            Text("Permitir acceso a cámara")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onClose) {
            Text("Cancelar")
        }
    }
}