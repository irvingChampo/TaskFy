package com.example.myapplication.src.Features.Task.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTaskScreen(
    taskId: Int = 0,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: String = "",
    initialPhotoUri: String? = null,
    onBackClick: () -> Unit = {},
    onUpdateTask: (Int, String, String, String, String?) -> Unit = { _, _, _, _, _ -> },
    onTakePhoto: () -> Unit = {},
    onSelectPhoto: () -> Unit = {}
) {
    var titulo by remember { mutableStateOf(initialTitle) }
    var descripcion by remember { mutableStateOf(initialDescription) }
    var fecha by remember { mutableStateOf(initialDate) }
    var selectedImageUri by remember { mutableStateOf(initialPhotoUri) }
    var showPhotoOptions by remember { mutableStateOf(false) }

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
            // Header
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

            // Contenido del formulario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Campo Título
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
                        value = titulo,
                        onValueChange = { titulo = it },
                        placeholder = {
                            Text(
                                text = "Título:",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
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

                // Campo Descripción
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
                        value = descripcion,
                        onValueChange = { descripcion = it },
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

                // Campo Fecha
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📅 Fecha",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        placeholder = {
                            Text(
                                text = "DD/MM/AAAA",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Sección de Foto
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
                            .clickable { showPhotoOptions = true },
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
                            if (selectedImageUri != null) {
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
                                        color = Color(0xFF28A745),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Toca para cambiar",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
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

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Actualizar
                Button(
                    onClick = {
                        onUpdateTask(taskId, titulo, descripcion, fecha, selectedImageUri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF28A745) // Verde para indicar actualización
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
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

                // Espacio adicional para scroll
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Modal para opciones de foto
        if (showPhotoOptions) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showPhotoOptions = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📷 Cambiar Foto",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Opción Cámara
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTakePhoto()
                                    selectedImageUri = "camera_photo_updated"
                                    showPhotoOptions = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📸",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "Tomar Nueva Foto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }

                        // Opción Galería
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectPhoto()
                                    selectedImageUri = "gallery_photo_updated"
                                    showPhotoOptions = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF28A745).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🖼️",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "Elegir de Galería",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF28A745)
                                )
                            }
                        }

                        // Opción Eliminar Foto (solo si hay foto)
                        if (selectedImageUri != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedImageUri = null
                                        showPhotoOptions = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFDC3545).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "🗑️",
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = "Eliminar Foto",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFDC3545)
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showPhotoOptions = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}