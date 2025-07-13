package com.example.myapplication.src.Features.Task.domain.usecase

import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        titulo: String,
        descripcion: String? = null,
        prioridad: String? = "media",
        estado: String? = "pendiente",
        fecha: String? = null,
        base64Image: String? = null
    ): Response<TaskResponse> {

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

        val fechaFormateada = fecha?.let { validateAndFormatDate(it) }

        return taskRepository.crearTarea(
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad,
            estado = estado,
            fecha = fechaFormateada,
            base64Image = base64Image
        )
    }

    private fun validateAndFormatDate(fecha: String): String {
        if (fecha.isBlank()) return ""

        return try {
            if (fecha.contains("T") && fecha.contains("Z")) {
                return fecha
            }

            if (fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                return fecha
            }

            if (fecha.contains("/")) {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                inputFormat.isLenient = false

                val date = inputFormat.parse(fecha)
                    ?: throw IllegalArgumentException("Formato de fecha inválido")

                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                return outputFormat.format(date)
            }

            throw IllegalArgumentException("Formato de fecha no reconocido: $fecha")

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Formato de fecha inválido. Use DD/MM/YYYY (ejemplo: 25/12/2024) o formato ISO")
        }
    }
}