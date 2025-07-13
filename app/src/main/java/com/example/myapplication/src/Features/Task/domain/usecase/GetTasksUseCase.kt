package com.example.myapplication.src.Features.Task.domain.usecase

import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response

class GetTasksUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(): Response<List<TaskResponse>> {
        return taskRepository.getTareas()
    }
}