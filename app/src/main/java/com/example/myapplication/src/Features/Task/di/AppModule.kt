package com.example.myapplication.src.Features.Task.di

import com.example.myapplication.src.Core.DI.HardwareModule
import com.example.myapplication.src.Core.Network.RetrofitHelper
import com.example.myapplication.src.Features.Task.data.datasource.remote.TaskService
import com.example.myapplication.src.Features.Task.data.repository.TaskRepositoryImpl
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import com.example.myapplication.src.Features.Task.domain.usecase.*

object AppModule {

    init {
        RetrofitHelper.init()
    }

    private val taskService: TaskService by lazy {
        RetrofitHelper.getService(TaskService::class.java)
    }

     val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(taskService)
    }

    val getTasksUseCase: GetTasksUseCase by lazy {
        GetTasksUseCase(taskRepository)
    }

    val getTaskByIdUseCase: GetTaskByIdUseCase by lazy {
        GetTaskByIdUseCase(taskRepository)
    }

    val createTaskUseCase: CreateTaskUseCase by lazy {
        CreateTaskUseCase(taskRepository)
    }

    val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(taskRepository)
    }

    val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(taskRepository)
    }

    val cameraRepository by lazy {
        HardwareModule.cameraRepository
    }

    val vibratorRepository by lazy {
        HardwareModule.vibratorRepository
    }
}