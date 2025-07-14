package com.example.myapplication.src.Features.Task.domain.repository

import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import retrofit2.Response

interface TaskRepository {
    suspend fun getTareas(): Response<List<TaskResponse>>
    suspend fun getTareaById(id: String): Response<TaskResponse>
    suspend fun crearTarea(
        titulo: String,
        descripcion: String?,
        prioridad: String?,
        estado: String?,
        fecha: String?,
        base64Image: String?
    ): Response<TaskResponse>
    suspend fun actualizarTarea(
        id: String,
        titulo: String,
        descripcion: String?,
        prioridad: String?,
        estado: String?,
        fecha: String?,
        base64Image: String?,
        eliminarImagen: Boolean?
    ): Response<TaskResponse>
    suspend fun eliminarTarea(id: String): Response<Unit>
    suspend fun syncPendingTasks()
    suspend fun getPendingTasksCount(): Int
    suspend fun getOfflineTasks(): List<TaskResponse>
}