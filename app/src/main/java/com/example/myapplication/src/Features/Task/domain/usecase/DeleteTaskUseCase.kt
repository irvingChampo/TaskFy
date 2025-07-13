package com.example.myapplication.src.Features.Task.domain.usecase

import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: String): Response<Unit> {
        if (id.isBlank()) {
            throw IllegalArgumentException("ID de tarea es requerido")
        }
        return taskRepository.eliminarTarea(id)
    }
}