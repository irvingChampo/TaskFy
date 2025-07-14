package com.example.myapplication.src.Features.Task.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.src.Features.Task.di.AppModule

class TaskViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(
                getTasksUseCase = AppModule.getTasksUseCase,
                getTaskByIdUseCase = AppModule.getTaskByIdUseCase,
                createTaskUseCase = AppModule.createTaskUseCase,
                updateTaskUseCase = AppModule.updateTaskUseCase,
                deleteTaskUseCase = AppModule.deleteTaskUseCase,
                cameraRepository = AppModule.cameraRepository,
                vibratorRepository = AppModule.vibratorRepository,
                taskRepository = AppModule.taskRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}