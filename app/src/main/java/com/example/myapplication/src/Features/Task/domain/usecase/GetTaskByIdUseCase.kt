package com.example.myapplication.src.Features.Task.domain.usecase

import android.util.Log
import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response

class GetTaskByIdUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: String): Response<TaskResponse> {
        Log.d("GetTaskByIdUseCase", "=== GetTaskByIdUseCase INICIADO ===")
        Log.d("GetTaskByIdUseCase", "ID recibido: '$id'")
        Log.d("GetTaskByIdUseCase", "TaskRepository instance: $taskRepository")

        return try {
            Log.d("GetTaskByIdUseCase", "Validando ID...")

            // Validaciones del ID
            if (id.isBlank()) {
                Log.e("GetTaskByIdUseCase", "ERROR: ID está vacío o en blanco")
                throw IllegalArgumentException("El ID de la tarea no puede estar vacío")
            }

            if (id.length != 24) {
                Log.w("GetTaskByIdUseCase", "ADVERTENCIA: ID no tiene el formato estándar de MongoDB (24 caracteres)")
                Log.w("GetTaskByIdUseCase", "ID length actual: ${id.length}")
            }

            Log.d("GetTaskByIdUseCase", "ID validado correctamente")
            Log.d("GetTaskByIdUseCase", "Llamando al repositorio...")

            val response = taskRepository.getTareaById(id)

            Log.d("GetTaskByIdUseCase", "=== RESPUESTA DEL REPOSITORIO ===")
            Log.d("GetTaskByIdUseCase", "Response recibida: $response")
            Log.d("GetTaskByIdUseCase", "Response code: ${response.code()}")
            Log.d("GetTaskByIdUseCase", "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val tarea = response.body()
                Log.d("GetTaskByIdUseCase", "=== ANÁLISIS DE LA TAREA ===")
                Log.d("GetTaskByIdUseCase", "Tarea es null: ${tarea == null}")

                if (tarea != null) {
                    Log.d("GetTaskByIdUseCase", "✅ Tarea obtenida exitosamente")
                    Log.d("GetTaskByIdUseCase", "- ID de la tarea: '${tarea._id}'")
                    Log.d("GetTaskByIdUseCase", "- Título: '${tarea.titulo}'")
                    Log.d("GetTaskByIdUseCase", "- Estado: '${tarea.estado}'")

                    // Verificar integridad de datos
                    val problemas = mutableListOf<String>()
                    if (tarea._id.isBlank()) problemas.add("ID vacío")
                    if (tarea.titulo.isBlank()) problemas.add("Título vacío")
                    if (tarea.fecha.isBlank()) problemas.add("Fecha vacía")
                    if (tarea.prioridad.isBlank()) problemas.add("Prioridad vacía")
                    if (tarea.estado.isBlank()) problemas.add("Estado vacío")

                    if (problemas.isNotEmpty()) {
                        Log.w("GetTaskByIdUseCase", "⚠️ PROBLEMAS DE DATOS DETECTADOS: $problemas")
                    } else {
                        Log.d("GetTaskByIdUseCase", "✅ Datos de la tarea están completos")
                    }
                } else {
                    Log.e("GetTaskByIdUseCase", "❌ Tarea es null a pesar del código 200")
                }
            } else {
                Log.e("GetTaskByIdUseCase", "❌ Error en la respuesta: ${response.code()}")
                val errorBody = response.errorBody()?.string()
                Log.e("GetTaskByIdUseCase", "Error body: $errorBody")
            }

            Log.d("GetTaskByIdUseCase", "Retornando response desde UseCase")
            response

        } catch (e: Exception) {
            Log.e("GetTaskByIdUseCase", "=== EXCEPCIÓN EN GetTaskByIdUseCase ===", e)
            Log.e("GetTaskByIdUseCase", "Tipo: ${e.javaClass.simpleName}")
            Log.e("GetTaskByIdUseCase", "Mensaje: ${e.message}")

            // Re-lanzar la excepción para que la maneje el ViewModel
            throw e
        }
    }
}