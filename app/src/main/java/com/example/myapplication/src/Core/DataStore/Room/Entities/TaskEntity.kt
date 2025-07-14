package com.example.myapplication.src.Core.Room.Entities

import androidx.room.*

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuario_id"]),
        Index(value = ["estado"]),
        Index(value = ["prioridad"]),
        Index(value = ["fecha"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "usuario_id")
    val usuarioId: String,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "fecha")
    val fecha: String,

    @ColumnInfo(name = "prioridad")
    val prioridad: TaskPrioridad = TaskPrioridad.MEDIA,

    @ColumnInfo(name = "estado")
    val estado: TaskEstado = TaskEstado.PENDIENTE,

    @ColumnInfo(name = "imagen_key")
    val imagenKey: String? = null,

    @ColumnInfo(name = "imagen_url")
    val imagenUrl: String? = null,

    @Embedded(prefix = "metadata_")
    val imagenMetadata: ImageMetadataEntity? = null,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "needs_upload")
    val needsUpload: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "server_created_at")
    val serverCreatedAt: String? = null,

    @ColumnInfo(name = "server_updated_at")
    val serverUpdatedAt: String? = null
)

data class ImageMetadataEntity(
    @ColumnInfo(name = "original_name")
    val originalName: String? = null,

    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,

    @ColumnInfo(name = "size")
    val size: Long? = null,

    @ColumnInfo(name = "uploaded_at")
    val uploadedAt: String? = null
)

enum class TaskPrioridad(val value: String) {
    ALTA("alta"),
    MEDIA("media"),
    BAJA("baja");

    companion object {
        fun fromString(value: String): TaskPrioridad {
            return values().find { it.value == value } ?: MEDIA
        }
    }
}

enum class TaskEstado(val value: String) {
    PENDIENTE("pendiente"),
    COMPLETADA("completada");

    companion object {
        fun fromString(value: String): TaskEstado {
            return values().find { it.value == value } ?: PENDIENTE
        }
    }
}