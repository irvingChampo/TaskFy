package com.example.myapplication.src.Features.Task.data.repository

import android.util.Log
import com.example.myapplication.src.Core.DI.DatabaseModule
import com.example.myapplication.src.Core.Room.Entities.*
import com.example.myapplication.src.Features.Task.data.datasource.remote.TaskService
import com.example.myapplication.src.Features.Task.data.model.*
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response
import java.util.UUID

class TaskRepositoryImpl(
    private val taskService: TaskService
) : TaskRepository {

    private val taskDao = DatabaseModule.taskDao
    private val userDao = DatabaseModule.userDao

    override suspend fun getTareas(): Response<List<TaskResponse>> {
        return try {
            val currentUserId = DatabaseModule.getCurrentUserId()
            if (currentUserId == null) {
                return Response.error(401,
                    okhttp3.ResponseBody.create(null, "No authenticated user"))
            }

            val cachedTasks = taskDao.getTasksByUser(currentUserId)
            Log.d("TaskRepository", "Tareas en cache: ${cachedTasks.size}")

            try {
                val apiResponse = taskService.getTareas()
                if (apiResponse.isSuccessful) {
                    val apiTasks = apiResponse.body()
                    apiTasks?.let { tasks ->
                        // Sincronizar tareas pendientes antes de limpiar
                        syncPendingTasks()

                        // Solo eliminar tareas sincronizadas, mantener las pendientes
                        val pendingTasks = taskDao.getTasksNeedingUpload()
                        taskDao.deleteAllTasksByUser(currentUserId)

                        // Insertar tareas del servidor + mantener pendientes
                        val taskEntities = tasks.map { it.toTaskEntity(currentUserId) }
                        taskDao.insertTasks(taskEntities + pendingTasks)
                    }
                    return apiResponse
                } else {
                    if (cachedTasks.isNotEmpty()) {
                        val cachedResponse = cachedTasks.map { it.toTaskResponse() }
                        return Response.success(cachedResponse)
                    }
                    return apiResponse
                }
            } catch (apiException: Exception) {
                Log.w("TaskRepository", "API no disponible, usando cache", apiException)
                if (cachedTasks.isNotEmpty()) {
                    val cachedResponse = cachedTasks.map { it.toTaskResponse() }
                    return Response.success(cachedResponse)
                }
                throw apiException
            }

        } catch (e: Exception) {
            try {
                val currentUserId = DatabaseModule.getCurrentUserId()
                if (currentUserId != null) {
                    val cachedTasks = taskDao.getTasksByUser(currentUserId)
                    if (cachedTasks.isNotEmpty()) {
                        val cachedResponse = cachedTasks.map { it.toTaskResponse() }
                        return Response.success(cachedResponse)
                    }
                }
            } catch (cacheException: Exception) {
                // Ignore cache errors
            }
            throw e
        }
    }

    override suspend fun getTareaById(id: String): Response<TaskResponse> {
        return try {
            val currentUserId = DatabaseModule.getCurrentUserId()
            if (currentUserId == null) {
                return Response.error(401,
                    okhttp3.ResponseBody.create(null, "No authenticated user"))
            }

            val cachedTask = taskDao.getTaskById(id)
            val apiResponse = taskService.getTareaById(id)

            if (apiResponse.isSuccessful) {
                val apiTask = apiResponse.body()
                apiTask?.let { task ->
                    val taskEntity = task.toTaskEntity(currentUserId)
                    taskDao.insertTask(taskEntity)
                }
                return apiResponse
            } else {
                cachedTask?.let { task ->
                    return Response.success(task.toTaskResponse())
                }
                return apiResponse
            }

        } catch (e: Exception) {
            try {
                val cachedTask = taskDao.getTaskById(id)
                cachedTask?.let { task ->
                    return Response.success(task.toTaskResponse())
                }
            } catch (cacheException: Exception) {
                // Ignore cache errors
            }
            throw e
        }
    }

    override suspend fun crearTarea(
        titulo: String,
        descripcion: String?,
        prioridad: String?,
        estado: String?,
        fecha: String?,
        base64Image: String?
    ): Response<TaskResponse> {
        return try {
            val currentUserId = DatabaseModule.getCurrentUserId()
            if (currentUserId == null) {
                return Response.error(401,
                    okhttp3.ResponseBody.create(null, "No authenticated user"))
            }

            // 1. SIEMPRE guardar en cache primero (offline-first)
            val tempId = "temp_${UUID.randomUUID()}"
            val currentTime = System.currentTimeMillis()

            val tempTaskEntity = TaskEntity(
                id = tempId,
                usuarioId = currentUserId,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha ?: currentTime.toString(),
                prioridad = TaskPrioridad.fromString(prioridad ?: "media"),
                estado = TaskEstado.fromString(estado ?: "pendiente"),
                isSynced = false,
                needsUpload = true,
                createdAt = currentTime,
                updatedAt = currentTime
            )

            taskDao.insertTask(tempTaskEntity)
            Log.d("TaskRepository", "✅ Tarea guardada en cache: $titulo (ID: $tempId)")

            // 2. Intentar subir a API
            val request = TaskRequest(
                titulo = titulo,
                descripcion = descripcion,
                prioridad = prioridad,
                estado = estado,
                fecha = fecha,
                base64Image = base64Image
            )

            try {
                val apiResponse = taskService.crearTarea(request)

                if (apiResponse.isSuccessful) {
                    val createdTask = apiResponse.body()
                    createdTask?.let { task ->
                        // Eliminar tarea temporal y insertar la real del servidor
                        taskDao.deleteTaskById(tempId)
                        val realTaskEntity = task.toTaskEntity(currentUserId)
                        taskDao.insertTask(realTaskEntity)
                        Log.d("TaskRepository", "✅ Tarea sincronizada con servidor: $titulo")
                    }
                    return apiResponse
                } else {
                    // API falló pero tarea ya está guardada offline
                    Log.w("TaskRepository", "API falló (${apiResponse.code()}), tarea disponible offline")
                    val offlineResponse = TaskResponse(
                        _id = tempId,
                        usuario_id = currentUserId,
                        titulo = titulo,
                        descripcion = descripcion,
                        fecha = fecha ?: "",
                        prioridad = prioridad ?: "media",
                        estado = estado ?: "pendiente",
                        createdAt = currentTime.toString(),
                        updatedAt = currentTime.toString()
                    )
                    return Response.success(offlineResponse)
                }
            } catch (apiException: Exception) {
                // Error de red, pero tarea ya está en cache
                Log.w("TaskRepository", "Error de red, tarea guardada offline: $titulo", apiException)
                val offlineResponse = TaskResponse(
                    _id = tempId,
                    usuario_id = currentUserId,
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fecha ?: "",
                    prioridad = prioridad ?: "media",
                    estado = estado ?: "pendiente",
                    createdAt = currentTime.toString(),
                    updatedAt = currentTime.toString()
                )
                return Response.success(offlineResponse)
            }

        } catch (e: Exception) {
            Log.e("TaskRepository", "Error crítico en crearTarea", e)
            throw e
        }
    }

    override suspend fun actualizarTarea(
        id: String,
        titulo: String,
        descripcion: String?,
        prioridad: String?,
        estado: String?,
        fecha: String?,
        base64Image: String?,
        eliminarImagen: Boolean?
    ): Response<TaskResponse> {
        return try {
            val currentUserId = DatabaseModule.getCurrentUserId()
            if (currentUserId == null) {
                return Response.error(401,
                    okhttp3.ResponseBody.create(null, "No authenticated user"))
            }

            val existingTask = taskDao.getTaskById(id)
            existingTask?.let { task ->
                val currentTime = System.currentTimeMillis()
                val updatedTask = task.copy(
                    titulo = titulo,
                    descripcion = descripcion,
                    prioridad = TaskPrioridad.fromString(prioridad ?: "media"),
                    estado = TaskEstado.fromString(estado ?: "pendiente"),
                    fecha = fecha ?: task.fecha,
                    needsUpload = true,
                    isSynced = false,
                    updatedAt = currentTime
                )
                taskDao.updateTask(updatedTask)
            }

            val request = TaskUpdateRequest(
                titulo = titulo,
                descripcion = descripcion,
                prioridad = prioridad,
                estado = estado,
                fecha = fecha,
                base64Image = base64Image,
                eliminarImagen = eliminarImagen
            )

            val apiResponse = taskService.actualizarTarea(id, request)

            if (apiResponse.isSuccessful) {
                taskDao.markTaskAsSynced(id)
                return apiResponse
            } else {
                existingTask?.let { task ->
                    val offlineResponse = task.copy(
                        titulo = titulo,
                        descripcion = descripcion,
                        fecha = fecha ?: task.fecha,
                        prioridad = TaskPrioridad.fromString(prioridad ?: "media"),
                        estado = TaskEstado.fromString(estado ?: "pendiente")
                    ).toTaskResponse()

                    return Response.success(offlineResponse)
                }
                return apiResponse
            }

        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun eliminarTarea(id: String): Response<Unit> {
        return try {
            val apiResponse = taskService.eliminarTarea(id)

            if (apiResponse.isSuccessful) {
                taskDao.deleteTaskById(id)
                return apiResponse
            } else {
                taskDao.deleteTaskById(id)
                return Response.success(Unit)
            }

        } catch (e: Exception) {
            try {
                taskDao.deleteTaskById(id)
                return Response.success(Unit)
            } catch (cacheException: Exception) {
                // Ignore cache errors
            }
            throw e
        }
    }

    // ✅ CORREGIDO: Con override
    override suspend fun syncPendingTasks() {
        try {
            val currentUserId = DatabaseModule.getCurrentUserId()
            if (currentUserId == null) {
                Log.w("TaskRepository", "No hay usuario para sincronizar")
                return
            }

            val pendingTasks = taskDao.getTasksNeedingUpload()
            Log.d("TaskRepository", "🔄 Sincronizando ${pendingTasks.size} tareas pendientes...")

            pendingTasks.forEach { task ->
                try {
                    Log.d("TaskRepository", "Sincronizando: ${task.titulo}")

                    // Crear request para la API
                    val request = TaskRequest(
                        titulo = task.titulo,
                        descripcion = task.descripcion,
                        prioridad = task.prioridad.value,
                        estado = task.estado.value,
                        fecha = task.fecha,
                        base64Image = null // Aquí manejarías imágenes si las hay
                    )

                    // Llamar a la API
                    val response = taskService.crearTarea(request)

                    if (response.isSuccessful) {
                        val serverTask = response.body()
                        if (serverTask != null) {
                            // Eliminar tarea temporal
                            taskDao.deleteTaskById(task.id)

                            // Insertar tarea real del servidor
                            val realTask = serverTask.toTaskEntity(currentUserId)
                            taskDao.insertTask(realTask)

                            Log.d("TaskRepository", "✅ Sincronizada: ${task.titulo}")
                        }
                    } else {
                        Log.e("TaskRepository", "❌ Error sincronizando ${task.titulo}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("TaskRepository", "❌ Excepción sincronizando ${task.titulo}", e)
                }
            }

            Log.d("TaskRepository", "🔄 Sincronización completada")

        } catch (e: Exception) {
            Log.e("TaskRepository", "Error general en syncPendingTasks", e)
        }
    }

    // ✅ CORREGIDO: Con override
    override suspend fun getOfflineTasks(): List<TaskResponse> {
        return try {
            val currentUserId = DatabaseModule.getCurrentUserId()
            if (currentUserId != null) {
                taskDao.getTasksByUser(currentUserId).map { it.toTaskResponse() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ CORREGIDO: Con override
    override suspend fun getPendingTasksCount(): Int {
        return try {
            taskDao.getTasksNeedingUpload().size
        } catch (e: Exception) {
            0
        }
    }
}

// Extension Functions para Mappers - Corregidas para API 24
fun TaskResponse.toTaskEntity(usuarioId: String): TaskEntity {
    val currentTime = System.currentTimeMillis()
    return TaskEntity(
        id = this.realId,
        usuarioId = usuarioId,
        titulo = this.realTitulo,
        descripcion = this.realDescripcion,
        fecha = this.realFecha,
        prioridad = TaskPrioridad.fromString(this.realPrioridad),
        estado = TaskEstado.fromString(this.realEstado),
        imagenKey = this.realImagenKey,
        imagenUrl = this.realImagenUrl,
        imagenMetadata = this.realImagenMetadata?.toImageMetadataEntity(),
        isSynced = true,
        needsUpload = false,
        serverCreatedAt = this.realCreatedAt,
        serverUpdatedAt = this.realUpdatedAt,
        createdAt = currentTime,
        updatedAt = currentTime
    )
}

fun TaskEntity.toTaskResponse(): TaskResponse {
    return TaskResponse(
        _id = this.id,
        usuario_id = this.usuarioId,
        titulo = this.titulo,
        descripcion = this.descripcion,
        fecha = this.fecha,
        prioridad = this.prioridad.value,
        estado = this.estado.value,
        imagen_key = this.imagenKey,
        imagen_url = this.imagenUrl,
        imagen_metadata = this.imagenMetadata?.toImageMetadata(),
        createdAt = this.serverCreatedAt ?: this.createdAt.toString(),
        updatedAt = this.serverUpdatedAt ?: this.updatedAt.toString()
    )
}

fun ImageMetadata.toImageMetadataEntity(): ImageMetadataEntity {
    return ImageMetadataEntity(
        originalName = this.originalName,
        mimeType = this.mimeType,
        size = this.size,
        uploadedAt = this.uploadedAt
    )
}

fun ImageMetadataEntity.toImageMetadata(): ImageMetadata {
    return ImageMetadata(
        originalName = this.originalName,
        mimeType = this.mimeType,
        size = this.size,
        uploadedAt = this.uploadedAt
    )
}