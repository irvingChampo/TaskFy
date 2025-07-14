package com.example.myapplication.src.Core.Room.Converters

import androidx.room.TypeConverter
import com.example.myapplication.src.Core.Room.Entities.TaskPrioridad
import com.example.myapplication.src.Core.Room.Entities.TaskEstado

class TaskConverters {

    @TypeConverter
    fun fromTaskPrioridad(value: TaskPrioridad): String = value.value

    @TypeConverter
    fun toTaskPrioridad(value: String): TaskPrioridad = TaskPrioridad.fromString(value)

    @TypeConverter
    fun fromTaskEstado(value: TaskEstado): String = value.value

    @TypeConverter
    fun toTaskEstado(value: String): TaskEstado = TaskEstado.fromString(value)
}