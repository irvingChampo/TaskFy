package com.example.myapplication.src.Features.Task.data.datasource.remote

import com.example.myapplication.src.Features.Task.data.model.TaskRequest
import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.data.model.TaskUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface TaskService {

    @GET("tasks")
    suspend fun getTareas(): Response<List<TaskResponse>>

    @GET("tasks/{id}")
    suspend fun getTareaById(@Path("id") id: String): Response<TaskResponse>

    @POST("tasks")
    suspend fun crearTarea(@Body request: TaskRequest): Response<TaskResponse>

    @PUT("tasks/{id}")
    suspend fun actualizarTarea(
        @Path("id") id: String,
        @Body request: TaskUpdateRequest
    ): Response<TaskResponse>

    @DELETE("tasks/{id}")
    suspend fun eliminarTarea(@Path("id") id: String): Response<Unit>
}