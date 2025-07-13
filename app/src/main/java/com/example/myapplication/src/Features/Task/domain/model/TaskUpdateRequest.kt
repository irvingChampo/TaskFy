package com.example.myapplication.src.Features.Task.domain.model

data class TaskUpdateRequest(
    val titulo: String,
    val descripcion: String? = null,
    val prioridad: String? = null,
    val estado: String? = null,
    val fecha: String? = null,
    val base64Image: String? = null,
    val eliminarImagen: Boolean? = null
)