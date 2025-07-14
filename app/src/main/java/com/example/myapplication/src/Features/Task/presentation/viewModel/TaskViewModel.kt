package com.example.myapplication.src.Features.Task.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.src.Core.Hardware.domain.CamaraRepository
import com.example.myapplication.src.Core.Hardware.domain.VibratorRepository
import android.util.Base64
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.domain.usecase.GetTasksUseCase
import com.example.myapplication.src.Features.Task.domain.usecase.GetTaskByIdUseCase
import com.example.myapplication.src.Features.Task.domain.usecase.CreateTaskUseCase
import com.example.myapplication.src.Features.Task.domain.usecase.UpdateTaskUseCase
import com.example.myapplication.src.Features.Task.domain.usecase.DeleteTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log

// ✅ CORREGIDO: Import de la interfaz (no la implementación)
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository

class TaskViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val cameraRepository: CamaraRepository,
    private val vibratorRepository: VibratorRepository,
    // ✅ CORREGIDO: Usar interfaz TaskRepository (no TaskRepositoryImpl)
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskResponse>>(emptyList())
    private val _selectedTask = MutableStateFlow<TaskResponse?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _operationSuccess = MutableStateFlow(false)
    // ✅ AGREGADO: Estado para mostrar info de sincronización
    private val _pendingTasksCount = MutableStateFlow(0)

    val tasks: StateFlow<List<TaskResponse>> = _tasks.asStateFlow()
    val selectedTask: StateFlow<TaskResponse?> = _selectedTask.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()
    val pendingTasksCount: StateFlow<Int> = _pendingTasksCount.asStateFlow()

    // ✅ AGREGADO: Sincronización automática al inicializar
    init {
        Log.d("TaskViewModel", "🚀 Inicializando TaskViewModel...")
        syncAndLoadTasks()
    }

    // ✅ AGREGADO: Método principal de sincronización + carga
    private fun syncAndLoadTasks() {
        viewModelScope.launch {
            try {
                Log.d("TaskViewModel", "🔄 Iniciando sincronización...")

                // 1. Sincronizar tareas pendientes en background
                taskRepository.syncPendingTasks()

                // 2. Actualizar contador de tareas pendientes
                updatePendingTasksCount()

                // 3. Cargar todas las tareas después de sincronizar
                getTareas()

                Log.d("TaskViewModel", "✅ Sincronización y carga completada")

            } catch (e: Exception) {
                Log.w("TaskViewModel", "⚠️ Error en sincronización inicial", e)
                // Aún así cargar tareas desde cache
                getTareas()
            }
        }
    }

    // ✅ AGREGADO: Actualizar contador de tareas pendientes
    private suspend fun updatePendingTasksCount() {
        try {
            val count = taskRepository.getPendingTasksCount()
            _pendingTasksCount.value = count
            Log.d("TaskViewModel", "📊 Tareas pendientes: $count")
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error obteniendo tareas pendientes", e)
        }
    }

    // ✅ MODIFICADO: Método público para refresh manual
    fun refreshTasks() {
        Log.d("TaskViewModel", "🔄 Refresh manual iniciado por usuario")
        syncAndLoadTasks()
    }

    // ✅ AGREGADO: Método para forzar sincronización manual
    fun forceSyncNow() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("TaskViewModel", "🔄 Sincronización manual forzada...")

                taskRepository.syncPendingTasks()
                updatePendingTasksCount()
                getTareas()

                Log.d("TaskViewModel", "✅ Sincronización manual completada")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Error en sincronización manual", e)
                _errorMessage.value = "Error en sincronización: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun convertDateFormat(dateString: String?): String? {
        if (dateString.isNullOrBlank()) return null

        return try {
            if (dateString.contains("T") || dateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                return dateString
            }

            if (dateString.contains("/")) {
                val parts = dateString.split("/")
                if (parts.size == 3) {
                    val day = parts[0].padStart(2, '0')
                    val month = parts[1].padStart(2, '0')
                    val year = parts[2]
                    return "$year-$month-${day}T00:00:00.000Z"
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    private fun compressImageToBase64(file: File): String? {
        try {
            if (!file.exists() || file.length() == 0L) {
                return null
            }

            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (originalBitmap == null) {
                return null
            }

            val maxDimension = 800
            val ratio = minOf(
                maxDimension.toFloat() / originalBitmap.width,
                maxDimension.toFloat() / originalBitmap.height,
                1.0f
            )

            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            originalBitmap.recycle()

            val outputStream = ByteArrayOutputStream()
            var quality = 80
            var compressedBytes: ByteArray

            do {
                outputStream.reset()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedBytes = outputStream.toByteArray()

                if (compressedBytes.size <= 1024 * 1024) break
                quality -= 10
            } while (quality > 30)

            resizedBitmap.recycle()
            outputStream.close()

            val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            return "data:image/jpeg;base64,$base64"

        } catch (e: OutOfMemoryError) {
            return null
        } catch (e: Exception) {
            return null
        }
    }

    fun getTareas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("TaskViewModel", "📋 Cargando tareas...")
                val response = getTasksUseCase()

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    _tasks.value = responseBody ?: emptyList()
                    Log.d("TaskViewModel", "✅ ${_tasks.value.size} tareas cargadas")

                    // ✅ AGREGADO: Actualizar contador después de cargar
                    updatePendingTasksCount()
                } else {
                    _errorMessage.value = when (response.code()) {
                        401 -> "Sesión expirada. Inicia sesión nuevamente"
                        403 -> "No tienes permisos para ver las tareas"
                        500 -> "Error del servidor"
                        else -> "Error al cargar tareas: ${response.code()}"
                    }
                    Log.e("TaskViewModel", "❌ Error cargando tareas: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("TaskViewModel", "❌ Excepción cargando tareas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTareaById(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                Log.d("TaskViewModel", "🔍 Buscando tarea: $id")
                val response = getTaskByIdUseCase(id)

                if (response.isSuccessful) {
                    val tarea = response.body()

                    if (tarea != null) {
                        if (tarea.tarea != null) {
                            val taskResponseFromData = TaskResponse(
                                _id = tarea.tarea._id,
                                usuario_id = tarea.tarea.usuario_id,
                                titulo = tarea.tarea.titulo,
                                descripcion = tarea.tarea.descripcion,
                                fecha = tarea.tarea.fecha,
                                prioridad = tarea.tarea.prioridad,
                                estado = tarea.tarea.estado,
                                imagen_key = tarea.tarea.imagen_key,
                                imagen_url = tarea.tarea.imagen_url,
                                imagen_metadata = tarea.tarea.imagen_metadata,
                                createdAt = tarea.tarea.createdAt,
                                updatedAt = tarea.tarea.updatedAt,
                                __v = tarea.tarea.__v
                            )
                            _selectedTask.value = taskResponseFromData
                        } else {
                            _selectedTask.value = tarea
                        }
                        Log.d("TaskViewModel", "✅ Tarea encontrada: ${tarea.titulo}")
                    } else {
                        _errorMessage.value = "Tarea no encontrada"
                        Log.w("TaskViewModel", "⚠️ Tarea no encontrada: $id")
                    }
                } else {
                    _errorMessage.value = when (response.code()) {
                        401 -> "Sesión expirada. Inicia sesión nuevamente"
                        404 -> "Tarea no encontrada"
                        else -> "Error al cargar tarea: ${response.code()}"
                    }
                    Log.e("TaskViewModel", "❌ Error buscando tarea: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("TaskViewModel", "❌ Excepción buscando tarea", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearTarea(
        titulo: String,
        descripcion: String? = null,
        prioridad: String? = "media",
        estado: String? = "pendiente",
        fecha: String? = null,
        capturedPhoto: File? = null
    ) {
        if (titulo.isBlank()) {
            _errorMessage.value = "El título es obligatorio"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _operationSuccess.value = false

            try {
                Log.d("TaskViewModel", "➕ Creando tarea: $titulo")

                val base64Image = capturedPhoto?.let { file ->
                    compressImageToBase64(file)
                }

                val fechaConvertida = convertDateFormat(fecha)

                val response = createTaskUseCase(titulo, descripcion, prioridad, estado, fechaConvertida, base64Image)

                if (response.isSuccessful) {
                    Log.d("TaskViewModel", "✅ Tarea creada: $titulo")
                    _operationSuccess.value = true

                    // ✅ MODIFICADO: Refresh completo (sync + load)
                    refreshTasks()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = when (response.code()) {
                        400 -> {
                            if (errorBody?.contains("base64", ignoreCase = true) == true) {
                                "Error en formato de imagen. Intenta sin foto."
                            } else if (errorBody?.contains("fecha", ignoreCase = true) == true) {
                                "Error en formato de fecha. Usa DD/MM/YYYY"
                            } else {
                                "Datos inválidos: $errorBody"
                            }
                        }
                        401 -> "Sesión expirada. Inicia sesión nuevamente"
                        422 -> "Error de validación: $errorBody"
                        500 -> "Error del servidor"
                        else -> "Error al crear tarea: ${response.code()}"
                    }
                    Log.e("TaskViewModel", "❌ Error creando tarea: ${response.code()}")
                }
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
                Log.e("TaskViewModel", "❌ Argumento inválido", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("TaskViewModel", "❌ Excepción creando tarea", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarTarea(
        id: String,
        titulo: String,
        descripcion: String? = null,
        prioridad: String? = null,
        estado: String? = null,
        fecha: String? = null,
        capturedPhoto: File? = null,
        eliminarImagen: Boolean? = null
    ) {
        if (id.isBlank()) {
            _errorMessage.value = "Error: ID de tarea inválido"
            return
        }

        if (titulo.isBlank()) {
            _errorMessage.value = "El título es obligatorio"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _operationSuccess.value = false

            try {
                Log.d("TaskViewModel", "✏️ Actualizando tarea: $titulo")

                val base64Image = capturedPhoto?.let { file ->
                    compressImageToBase64(file)
                }

                val fechaConvertida = convertDateFormat(fecha)

                if (base64Image != null && !base64Image.startsWith("data:image/")) {
                    _errorMessage.value = "Error en formato de imagen"
                    return@launch
                }

                val response = updateTaskUseCase(
                    id = id,
                    titulo = titulo,
                    descripcion = descripcion,
                    prioridad = prioridad,
                    estado = null,
                    fecha = fechaConvertida,
                    base64Image = base64Image,
                    eliminarImagen = eliminarImagen
                )

                if (response.isSuccessful) {
                    Log.d("TaskViewModel", "✅ Tarea actualizada: $titulo")
                    _operationSuccess.value = true

                    // ✅ MODIFICADO: Refresh completo (sync + load)
                    refreshTasks()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = when (response.code()) {
                        400 -> {
                            if (errorBody?.contains("base64", ignoreCase = true) == true) {
                                "Error en formato de imagen. Intenta sin foto."
                            } else if (errorBody?.contains("fecha", ignoreCase = true) == true) {
                                "Error en formato de fecha. Usa DD/MM/YYYY"
                            } else {
                                "Datos inválidos: $errorBody"
                            }
                        }
                        401 -> "Sesión expirada. Inicia sesión nuevamente"
                        404 -> "Tarea no encontrada"
                        422 -> "Error de validación: $errorBody"
                        500 -> {
                            if (errorBody?.contains("base64", ignoreCase = true) == true) {
                                "Error del servidor procesando imagen. Intenta sin foto."
                            } else {
                                "Error del servidor"
                            }
                        }
                        else -> "Error al actualizar tarea: ${response.code()}"
                    }
                    Log.e("TaskViewModel", "❌ Error actualizando tarea: ${response.code()}")
                }
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
                Log.e("TaskViewModel", "❌ Argumento inválido", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("TaskViewModel", "❌ Excepción actualizando tarea", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarTarea(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _operationSuccess.value = false

            try {
                Log.d("TaskViewModel", "🗑️ Eliminando tarea: $id")
                val response = deleteTaskUseCase(id)

                if (response.isSuccessful) {
                    Log.d("TaskViewModel", "✅ Tarea eliminada: $id")
                    vibratorRepository.vibrateShort()
                    _operationSuccess.value = true

                    // ✅ MODIFICADO: Refresh completo (sync + load)
                    refreshTasks()
                } else {
                    _errorMessage.value = when (response.code()) {
                        401 -> "Sesión expirada. Inicia sesión nuevamente"
                        404 -> "Tarea no encontrada"
                        else -> "Error al eliminar tarea: ${response.code()}"
                    }
                    Log.e("TaskViewModel", "❌ Error eliminando tarea: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("TaskViewModel", "❌ Excepción eliminando tarea", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCameraRepository(): CamaraRepository {
        return cameraRepository
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearOperationSuccess() {
        _operationSuccess.value = false
    }
}