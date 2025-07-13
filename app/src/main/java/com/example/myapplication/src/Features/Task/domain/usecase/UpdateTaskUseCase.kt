package com.example.myapplication.src.Features.Task.domain.usecase

import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        id: String,
        titulo: String,
        descripcion: String? = null,
        prioridad: String? = null,
        estado: String? = null,
        fecha: String? = null,
        base64Image: String? = null,
        eliminarImagen: Boolean? = null
    ): Response<TaskResponse> {
        if (id.isBlank()) {
            throw IllegalArgumentException("ID de tarea es requerido")
        }
        if (titulo.isBlank()) {
            throw IllegalArgumentException("Título es requerido")
        }

        prioridad?.let {
            if (it !in listOf("alta", "media", "baja")) {
                throw IllegalArgumentException("Prioridad debe ser: alta, media, baja")
            }
        }

        estado?.let {
            if (it !in listOf("pendiente", "completada")) {
                throw IllegalArgumentException("Estado debe ser: pendiente, completada")
            }
        }

        return taskRepository.actualizarTarea(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad,
            estado = estado,
            fecha = fecha,
            base64Image = base64Image,
            eliminarImagen = eliminarImagen
        )
    }
}