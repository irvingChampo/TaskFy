package com.example.myapplication.src.Features.Task.presentation.view

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.src.Features.Task.presentation.viewmodel.TaskViewModel
import com.example.myapplication.src.Features.Task.presentation.viewmodel.TaskViewModelFactory
import java.io.File
import java.util.*

private sealed class ImageState {
    object Loading : ImageState()
    data class Success(val bitmap: androidx.compose.ui.graphics.ImageBitmap) : ImageState()
    data class Error(val message: String) : ImageState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTaskScreen(
    taskId: String,
    onBackClick: () -> Unit = {},
    onTaskUpdated: () -> Unit = {},
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory())
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if (taskId.isBlank()) {
        LaunchedEffect(Unit) {
            android.widget.Toast.makeText(context, "Error: ID de tarea inválido", android.widget.Toast.LENGTH_LONG).show()
            onBackClick()
        }
        return
    }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("media") }
    var capturedPhoto by remember { mutableStateOf<File?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    var imagenUrlOriginal by remember { mutableStateOf<String?>(null) }

    val isLoading by taskViewModel.isLoading.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    val operationSuccess by taskViewModel.operationSuccess.collectAsState()
    val selectedTask by taskViewModel.selectedTask.collectAsState()

    LaunchedEffect(taskId) {
        try {
            taskViewModel.getTareaById(taskId)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error al cargar tarea: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(selectedTask) {
        val currentTask = selectedTask
        if (currentTask != null) {
            try {
                titulo = currentTask.realTitulo
                descripcion = currentTask.realDescripcion ?: ""
                fecha = currentTask.realFecha
                prioridad = currentTask.realPrioridad.ifBlank { "media" }
                imagenUrlOriginal = currentTask.realImagenUrl
            } catch (e: Exception) {
                // Error en actualización de campos
            }
        }
    }

    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            try {
                taskViewModel.clearOperationSuccess()
                android.widget.Toast.makeText(context, "Tarea actualizada exitosamente", android.widget.Toast.LENGTH_SHORT).show()
                onTaskUpdated()
            } catch (e: Exception) {
                // Error en manejo de éxito
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            try {
                android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                taskViewModel.clearError()
            } catch (e: Exception) {
                // Error al manejar error
            }
        }
    }

    LaunchedEffect(titulo, descripcion, fecha, prioridad) {
        if (errorMessage != null) {
            taskViewModel.clearError()
        }
    }

    if (showCamera) {
        CamaraActualizar(
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
        UpdateTaskContent(
            titulo = titulo,
            onTituloChange = { titulo = it },
            descripcion = descripcion,
            onDescripcionChange = { descripcion = it },
            fecha = fecha,
            onFechaChange = { fecha = it },
            prioridad = prioridad,
            onPrioridadChange = { prioridad = it },
            capturedPhoto = capturedPhoto,
            onCameraClick = { showCamera = true },
            onRemovePhoto = { capturedPhoto = null },
            onBackClick = onBackClick,
            onUpdateClick = {
                try {
                    if (titulo.isBlank()) {
                        android.widget.Toast.makeText(context, "El título es obligatorio", android.widget.Toast.LENGTH_SHORT).show()
                        return@UpdateTaskContent
                    }

                    val currentTask = selectedTask
                    if (currentTask == null || currentTask.realTitulo.isBlank()) {
                        android.widget.Toast.makeText(context, "Error: Tarea no cargada. Intenta recargar.", android.widget.Toast.LENGTH_LONG).show()
                        return@UpdateTaskContent
                    }

                    taskViewModel.actualizarTarea(
                        id = taskId,
                        titulo = titulo,
                        descripcion = descripcion.ifBlank { null },
                        prioridad = prioridad,
                        fecha = fecha,
                        capturedPhoto = capturedPhoto
                    )

                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Error inesperado: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            },
            isLoading = isLoading,
            errorMessage = errorMessage,
            hasExistingPhoto = imagenUrlOriginal != null,
            isDataLoaded = run {
                val currentTask = selectedTask
                currentTask != null && currentTask.realTitulo.isNotBlank()
            }
        )
    }
}

@Composable
private fun UpdateTaskContent(
    titulo: String,
    onTituloChange: (String) -> Unit,
    descripcion: String,
    onDescripcionChange: (String) -> Unit,
    fecha: String,
    onFechaChange: (String) -> Unit,
    prioridad: String,
    onPrioridadChange: (String) -> Unit,
    capturedPhoto: File?,
    onCameraClick: () -> Unit,
    onRemovePhoto: () -> Unit,
    onBackClick: () -> Unit,
    onUpdateClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    hasExistingPhoto: Boolean,
    isDataLoaded: Boolean = true
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
            UpdateTaskHeader(onBackClick = onBackClick)

            if (!isDataLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3),
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "Cargando datos de la tarea...",
                            color = Color(0xFF6C757D),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TituloFieldUpdate(
                        value = titulo,
                        onValueChange = onTituloChange
                    )

                    DescripcionFieldUpdate(
                        value = descripcion,
                        onValueChange = onDescripcionChange
                    )

                    FechaFieldWithDatePickerUpdate(
                        value = fecha,
                        onValueChange = onFechaChange
                    )

                    PrioridadFieldUpdate(
                        value = prioridad,
                        onValueChange = onPrioridadChange
                    )

                    FotoSectionWithPreviewUpdate(
                        capturedPhoto = capturedPhoto,
                        hasExistingPhoto = hasExistingPhoto,
                        onCameraClick = onCameraClick,
                        onRemovePhoto = onRemovePhoto
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    UpdateButton(
                        onClick = onUpdateClick,
                        enabled = !isLoading && titulo.isNotBlank(),
                        isLoading = isLoading
                    )

                    errorMessage?.let { error ->
                        ErrorCardUpdate(error = error)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun UpdateTaskHeader(
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
                    text = "Editar tarea",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Actualiza la información",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun TituloFieldUpdate(
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
private fun DescripcionFieldUpdate(
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
private fun FechaFieldWithDatePickerUpdate(
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
                if (value.contains("/")) {
                    val parts = value.split("/")
                    if (parts.size == 3) {
                        calendar.set(
                            parts[2].toInt(),
                            parts[1].toInt() - 1,
                            parts[0].toInt()
                        )
                    }
                } else if (value.contains("-")) {
                    val parts = value.split("T")[0].split("-")
                    if (parts.size == 3) {
                        calendar.set(
                            parts[0].toInt(),
                            parts[1].toInt() - 1,
                            parts[2].toInt()
                        )
                    }
                }
            } catch (e: Exception) {
                // Error al parsear fecha
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
private fun PrioridadFieldUpdate(
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

@Composable
private fun FotoSectionWithPreviewUpdate(
    capturedPhoto: File?,
    hasExistingPhoto: Boolean,
    onCameraClick: () -> Unit,
    onRemovePhoto: () -> Unit
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

        when {
            capturedPhoto != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📸 Nueva foto capturada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF28A745)
                        )

                        ImagePreview(
                            photoFile = capturedPhoto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8F9FA))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCameraClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF2196F3)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF2196F3))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Cambiar",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cambiar")
                            }

                            OutlinedButton(
                                onClick = onRemovePhoto,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC3545)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFDC3545))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }

            hasExistingPhoto -> {
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
                                color = Color(0xFF2196F3),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📸",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "Foto actual",
                                color = Color(0xFF2196F3),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Toca para cambiar",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            else -> {
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "Toca para agregar foto",
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
}

@Composable
private fun ImagePreview(
    photoFile: File,
    modifier: Modifier = Modifier
) {
    var imageState by remember(photoFile) { mutableStateOf<ImageState>(ImageState.Loading) }

    LaunchedEffect(photoFile) {
        imageState = ImageState.Loading
        try {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageState = if (bitmap != null) {
                ImageState.Success(bitmap.asImageBitmap())
            } else {
                ImageState.Error("No se pudo cargar la imagen")
            }
        } catch (e: Exception) {
            imageState = ImageState.Error(e.message ?: "Error desconocido")
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (val state = imageState) {
            is ImageState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFF2196F3)
                )
            }
            is ImageState.Success -> {
                Image(
                    bitmap = state.bitmap,
                    contentDescription = "Foto capturada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            is ImageState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📸",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "Error al cargar imagen",
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateButton(
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
            containerColor = Color(0xFF28A745)
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
                    text = "✏️",
                    fontSize = 18.sp
                )
                Text(
                    text = "Actualizar Tarea",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorCardUpdate(error: String) {
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
fun CamaraActualizar(
    taskViewModel: TaskViewModel,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
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
                        try {
                            cameraRepository.initializeCamera(this, lifecycleOwner)
                        } catch (e: Exception) {
                            // Error al inicializar cámara
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            CameraControlsUpdate(
                cameraRepository = cameraRepository,
                onClose = onClose,
                onPhotoCaptured = onPhotoCaptured,
                isCapturing = isCapturing,
                onCapturingChange = { isCapturing = it },
                onError = { showError = it }
            )
        } else {
            CameraPermissionScreenUpdate(
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
private fun CameraControlsUpdate(
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
                onClick = {
                    try {
                        cameraRepository.switchCamera()
                    } catch (e: Exception) {
                        onError("Error al cambiar cámara")
                    }
                },
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

                        try {
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
                        } catch (e: Exception) {
                            onCapturingChange(false)
                            onError("Error crítico: ${e.message}")
                        }
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
private fun CameraPermissionScreenUpdate(
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
        TextButton(
            onClick = onClose
        ) {
            Text("Cancelar")
        }
    }
}