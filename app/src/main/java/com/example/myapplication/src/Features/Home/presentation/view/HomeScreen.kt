package com.example.myapplication.src.Features.Home.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToUpdateTask: (Int) -> Unit = {}
) {
    // Estado de ejemplo para las tareas
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Estudiar", "Estudiar para aprobar la materia", "30/05/2025", false),
                Task(2, "Estudiar", "Estudiar para aprobar la materia", "30/05/2025", true),
                Task(3, "Estudiar", "Estudiar para aprobar la materia", "30/05/2025", false)
            )
        )
    }

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
            // Header con gradiente y sombra
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "TaskFy",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Organiza tu día",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Indicadores de color con mejor diseño
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Indicador Pendientes
                    ColorIndicator(
                        text = "Pendientes",
                        backgroundColor = Color(0xFF6C757D),
                        icon = "⏳"
                    )

                    // Indicador Completado
                    ColorIndicator(
                        text = "Completado",
                        backgroundColor = Color(0xFF28A745),
                        icon = "✅"
                    )
                }
            }

            // Título de la lista con mejor estilo
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Lista de tareas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )
            }

            // Lista de tareas con mejor espaciado
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onEditClick = {
                            onNavigateToUpdateTask(task.id)
                        },
                        onDeleteClick = {
                            // Lógica para eliminar tarea
                            tasks = tasks.filter { it.id != task.id }
                        },
                        onToggleComplete = {
                            // Lógica para completar/descompletar tarea
                            tasks = tasks.map {
                                if (it.id == task.id) it.copy(isCompleted = !it.isCompleted)
                                else it
                            }
                        }
                    )
                }
            }
        }

        // Botón flotante con gradiente y sombra
        FloatingActionButton(
            onClick = onNavigateToCreateTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(64.dp),
            containerColor = Color(0xFF2196F3),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar tarea",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ColorIndicator(
    text: String,
    backgroundColor: Color,
    icon: String
) {
    Row(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                Color(0xFF28A745)
            else
                Color(0xFF6C757D)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box {
            // Gradiente sutil sobre la tarjeta
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header con título, fecha e icono de estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (task.isCompleted) "✅" else "📋",
                            fontSize = 20.sp
                        )
                        Text(
                            text = task.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = task.date,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción con mejor formato
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📝 Descripción:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            fontSize = 14.sp,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción con mejor diseño
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onEditClick() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onDeleteClick() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}