package com.example.myapplication.src.Features.Task.data.model

data class TaskResponse(
    // Campos para cuando viene directamente (como en lista de tareas)
    val _id: String = "",
    val usuario_id: String = "",
    val titulo: String = "",
    val descripcion: String? = null,
    val fecha: String = "",
    val prioridad: String = "",
    val estado: String = "",
    val imagen_key: String? = null,
    val imagen_url: String? = null,
    val imagen_metadata: ImageMetadata? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val __v: Int? = null,

    // Campos para respuesta anidada (getTareaById)
    val success: Boolean? = null,
    val mensaje: String? = null,
    val tarea: TaskData? = null
) {
    // SOLUCIÓN: Propiedades que devuelven los datos correctos automáticamente
    val realId: String
        get() = tarea?._id ?: _id

    val realTitulo: String
        get() = tarea?.titulo ?: titulo

    val realDescripcion: String?
        get() = tarea?.descripcion ?: descripcion

    val realFecha: String
        get() = tarea?.fecha ?: fecha

    val realPrioridad: String
        get() = tarea?.prioridad ?: prioridad

    val realEstado: String
        get() = tarea?.estado ?: estado

    val realImagenUrl: String?
        get() = tarea?.imagen_url ?: imagen_url

    val realImagenKey: String?
        get() = tarea?.imagen_key ?: imagen_key

    val realImagenMetadata: ImageMetadata?
        get() = tarea?.imagen_metadata ?: imagen_metadata

    val realCreatedAt: String
        get() = tarea?.createdAt ?: createdAt

    val realUpdatedAt: String
        get() = tarea?.updatedAt ?: updatedAt

    val realUsuarioId: String
        get() = tarea?.usuario_id ?: usuario_id
}

// Modelo para la tarea anidada
data class TaskData(
    val _id: String,
    val usuario_id: String,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    val prioridad: String,
    val estado: String,
    val imagen_key: String? = null,
    val imagen_url: String? = null,
    val imagen_metadata: ImageMetadata? = null,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int? = null
)

data class ImageMetadata(
    val originalName: String?,
    val mimeType: String?,
    val size: Long?,
    val uploadedAt: String?
)

enum class TaskPriority(val value: String) {
    ALTA("alta"),
    MEDIA("media"),
    BAJA("baja")
}

enum class TaskStatus(val value: String) {
    PENDIENTE("pendiente"),
    COMPLETADA("completada")
}