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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.src.Features.Task.presentation.viewmodel.TaskViewModel
import com.example.myapplication.src.Features.Task.presentation.viewmodel.TaskViewModelFactory
import com.example.myapplication.src.Features.Task.data.model.TaskResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToUpdateTask: (String) -> Unit = {},
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory())
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    val operationSuccess by taskViewModel.operationSuccess.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("todas") }

    val filteredTasks = when (selectedFilter) {
        "pendientes" -> tasks.filter { it.estado == "pendiente" }
        "completadas" -> tasks.filter { it.estado == "completada" }
        else -> tasks
    }

    LaunchedEffect(Unit) {
        taskViewModel.getTareas()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            taskViewModel.clearError()
        }
    }

    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            taskViewModel.clearOperationSuccess()
        }
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
            HomeHeader()

            FilterSection(
                selectedFilter = selectedFilter,
                onFilterChanged = { selectedFilter = it },
                allCount = tasks.size,
                pendingCount = tasks.count { it.estado == "pendiente" },
                completedCount = tasks.count { it.estado == "completada" }
            )

            TaskListHeader(taskCount = filteredTasks.size, filterText = getFilterText(selectedFilter))

            when {
                isLoading -> {
                    LoadingContent()
                }
                filteredTasks.isEmpty() -> {
                    EmptyTasksContent(filterType = selectedFilter)
                }
                else -> {
                    TaskListContent(
                        tasks = filteredTasks,
                        onEditClick = { taskId ->
                            onNavigateToUpdateTask(taskId)
                        },
                        onDeleteClick = { taskId ->
                            taskViewModel.eliminarTarea(taskId)
                        },
                        onToggleComplete = { task ->
                            val newStatus = if (task.estado == "completada") "pendiente" else "completada"
                            taskViewModel.actualizarTarea(
                                id = task._id,
                                titulo = task.titulo,
                                descripcion = task.descripcion,
                                prioridad = task.prioridad,
                                estado = newStatus,
                                fecha = task.fecha
                            )
                        }
                    )
                }
            }
        }

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
private fun HomeHeader() {
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
}

@Composable
private fun FilterSection(
    selectedFilter: String,
    onFilterChanged: (String) -> Unit,
    allCount: Int,
    pendingCount: Int,
    completedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Filtrar tareas",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterButton(
                    text = "Todas ($allCount)",
                    isSelected = selectedFilter == "todas",
                    onClick = { onFilterChanged("todas") },
                    icon = "📋",
                    modifier = Modifier.weight(1f)
                )

                FilterButton(
                    text = "Pendientes ($pendingCount)",
                    isSelected = selectedFilter == "pendientes",
                    onClick = { onFilterChanged("pendientes") },
                    icon = "⏳",
                    modifier = Modifier.weight(1f)
                )

                FilterButton(
                    text = "Completadas ($completedCount)",
                    isSelected = selectedFilter == "completadas",
                    onClick = { onFilterChanged("completadas") },
                    icon = "✅",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF6C757D),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun TaskListHeader(taskCount: Int, filterText: String) {
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
            text = "$filterText ($taskCount)",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C3E50)
        )
    }
}

private fun getFilterText(filter: String): String {
    return when (filter) {
        "pendientes" -> "Tareas pendientes"
        "completadas" -> "Tareas completadas"
        else -> "Todas las tareas"
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2196F3),
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
private fun EmptyTasksContent(filterType: String) {
    val (emoji, title, subtitle) = when (filterType) {
        "pendientes" -> Triple("⏳", "No hay tareas pendientes", "¡Excelente trabajo!")
        "completadas" -> Triple("✅", "No hay tareas completadas", "¡Comienza a completar tareas!")
        else -> Triple("📝", "No tienes tareas", "¡Agrega tu primera tarea!")
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emoji,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6C757D)
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF6C757D)
                )
            }
        }
    }
}

@Composable
private fun TaskListContent(
    tasks: List<TaskResponse>,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onToggleComplete: (TaskResponse) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onEditClick = {
                    onEditClick(task._id)
                },
                onDeleteClick = {
                    onDeleteClick(task._id)
                },
                onToggleComplete = {
                    onToggleComplete(task)
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: TaskResponse,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val isCompleted = task.estado == "completada"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp)
        ) {
            task.imagen_url?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen de tarea",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                            text = if (isCompleted) "✅" else "📋",
                            fontSize = 16.sp
                        )
                        Text(
                            text = task.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityChip(prioridad = task.prioridad)
                    StateChip(estado = task.estado)
                }

                task.descripcion?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        )
                    ) {
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D),
                            modifier = Modifier.padding(8.dp),
                            maxLines = 2
                        )
                    }
                }

                if (task.fecha.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.fecha,
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier
                            .size(36.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDC3545)
                        ),
                        modifier = Modifier
                            .size(36.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(prioridad: String) {
    val (icon, color) = when (prioridad.lowercase()) {
        "alta" -> "🔴" to Color(0xFFDC3545)
        "media" -> "🟡" to Color(0xFFFFC107)
        "baja" -> "🟢" to Color(0xFF28A745)
        else -> "⚪" to Color(0xFF6C757D)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
            Text(
                text = prioridad.uppercase(),
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StateChip(estado: String) {
    val (icon, color) = when (estado.lowercase()) {
        "completada" -> "✅" to Color(0xFF28A745)
        "pendiente" -> "⏳" to Color(0xFF6C757D)
        else -> "❓" to Color(0xFF6C757D)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
            Text(
                text = estado.uppercase(),
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}