package com.example.myapplication.src.Features.Task.data.repository

import android.util.Log
import com.example.myapplication.src.Features.Task.data.datasource.remote.TaskService
import com.example.myapplication.src.Features.Task.data.model.TaskRequest
import com.example.myapplication.src.Features.Task.data.model.TaskResponse
import com.example.myapplication.src.Features.Task.data.model.TaskUpdateRequest
import com.example.myapplication.src.Features.Task.domain.repository.TaskRepository
import retrofit2.Response

class TaskRepositoryImpl(
    private val taskService: TaskService
) : TaskRepository {

    override suspend fun getTareas(): Response<List<TaskResponse>> {
        Log.d("TaskRepository", "=== getTareas REPOSITORIO ===")
        return try {
            Log.d("TaskRepository", "Llamando a taskService.getTareas()...")
            val response = taskService.getTareas()

            Log.d("TaskRepository", "Respuesta de getTareas recibida:")
            Log.d("TaskRepository", "- Code: ${response.code()}")
            Log.d("TaskRepository", "- IsSuccessful: ${response.isSuccessful}")
            Log.d("TaskRepository", "- Message: ${response.message()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("TaskRepository", "Body de getTareas: ${body?.size ?: 0} elementos")
                body?.take(3)?.forEachIndexed { index, task ->
                    Log.d("TaskRepository", "Tarea $index: ID='${task._id}', Título='${task.titulo}'")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TaskRepository", "Error en getTareas: $errorBody")
            }

            response
        } catch (e: Exception) {
            Log.e("TaskRepository", "Excepción en getTareas", e)
            throw e
        }
    }

    override suspend fun getTareaById(id: String): Response<TaskResponse> {
        Log.d("TaskRepository", "=== getTareaById REPOSITORIO ===")
        Log.d("TaskRepository", "ID recibido: '$id'")
        Log.d("TaskRepository", "ID length: ${id.length}")
        Log.d("TaskRepository", "TaskService instance: $taskService")

        return try {
            // Construir la URL que se va a llamar
            Log.d("TaskRepository", "Construyendo URL para endpoint...")
            Log.d("TaskRepository", "Endpoint será: tasks/$id")

            Log.d("TaskRepository", "Llamando a taskService.getTareaById('$id')...")
            val response = taskService.getTareaById(id)

            Log.d("TaskRepository", "=== RESPUESTA DE TASK SERVICE ===")
            Log.d("TaskRepository", "Response object: $response")
            Log.d("TaskRepository", "Response code: ${response.code()}")
            Log.d("TaskRepository", "Response isSuccessful: ${response.isSuccessful}")
            Log.d("TaskRepository", "Response message: '${response.message()}'")
            Log.d("TaskRepository", "Response headers: ${response.headers()}")

            if (response.isSuccessful) {
                val tarea = response.body()
                Log.d("TaskRepository", "=== ANÁLISIS DEL BODY ===")
                Log.d("TaskRepository", "Body es null: ${tarea == null}")

                if (tarea != null) {
                    Log.d("TaskRepository", "=== TAREA ENCONTRADA EN REPOSITORIO ===")
                    Log.d("TaskRepository", "Objeto TaskResponse completo: $tarea")

                    // Log detallado de cada campo
                    Log.d("TaskRepository", "Campos individuales:")
                    Log.d("TaskRepository", "- _id: '${tarea._id}' (length: ${tarea._id.length})")
                    Log.d("TaskRepository", "- usuario_id: '${tarea.usuario_id}'")
                    Log.d("TaskRepository", "- titulo: '${tarea.titulo}' (length: ${tarea.titulo?.length ?: 0})")
                    Log.d("TaskRepository", "- descripcion: '${tarea.descripcion}' (length: ${tarea.descripcion?.length ?: 0})")
                    Log.d("TaskRepository", "- fecha: '${tarea.fecha}' (length: ${tarea.fecha?.length ?: 0})")
                    Log.d("TaskRepository", "- prioridad: '${tarea.prioridad}' (length: ${tarea.prioridad?.length ?: 0})")
                    Log.d("TaskRepository", "- estado: '${tarea.estado}' (length: ${tarea.estado?.length ?: 0})")
                    Log.d("TaskRepository", "- imagen_key: '${tarea.imagen_key}'")
                    Log.d("TaskRepository", "- imagen_url: '${tarea.imagen_url}'")
                    Log.d("TaskRepository", "- imagen_metadata: ${tarea.imagen_metadata}")
                    Log.d("TaskRepository", "- createdAt: '${tarea.createdAt}'")
                    Log.d("TaskRepository", "- updatedAt: '${tarea.updatedAt}'")

                    // Verificar campos adicionales de respuesta anidada
                    Log.d("TaskRepository", "Campos de respuesta anidada:")
                    Log.d("TaskRepository", "- success: ${tarea.success}")
                    Log.d("TaskRepository", "- mensaje: '${tarea.mensaje}'")
                    Log.d("TaskRepository", "- tarea anidada: ${tarea.tarea}")

                    if (tarea.tarea != null) {
                        Log.w("TaskRepository", "¡ATENCIÓN! Respuesta tiene estructura anidada")
                        Log.d("TaskRepository", "Datos de tarea anidada:")
                        Log.d("TaskRepository", "- ID anidado: '${tarea.tarea._id}'")
                        Log.d("TaskRepository", "- Título anidado: '${tarea.tarea.titulo}'")
                    }

                    // Verificar si los campos principales están vacíos/null
                    val camposVacios = mutableListOf<String>()
                    if (tarea._id.isBlank()) camposVacios.add("_id")
                    if (tarea.titulo.isBlank()) camposVacios.add("titulo")
                    if (tarea.fecha.isBlank()) camposVacios.add("fecha")
                    if (tarea.prioridad.isBlank()) camposVacios.add("prioridad")
                    if (tarea.estado.isBlank()) camposVacios.add("estado")

                    if (camposVacios.isNotEmpty()) {
                        Log.w("TaskRepository", "ADVERTENCIA: Campos vacíos detectados: $camposVacios")
                    } else {
                        Log.d("TaskRepository", "✅ Todos los campos principales tienen datos")
                    }

                } else {
                    Log.e("TaskRepository", "ERROR CRÍTICO: Response body es null pero código fue 200")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TaskRepository", "=== ERROR HTTP EN getTareaById ===")
                Log.e("TaskRepository", "Código de error: ${response.code()}")
                Log.e("TaskRepository", "Mensaje de error: ${response.message()}")
                Log.e("TaskRepository", "Error body: $errorBody")
                Log.e("TaskRepository", "Headers de error: ${response.headers()}")

                // Análisis específico por código de error
                when (response.code()) {
                    404 -> Log.e("TaskRepository", "ERROR 404: La tarea con ID '$id' no existe en el servidor")
                    401 -> Log.e("TaskRepository", "ERROR 401: No autorizado - verificar autenticación")
                    500 -> Log.e("TaskRepository", "ERROR 500: Error interno del servidor")
                    else -> Log.e("TaskRepository", "ERROR ${response.code()}: Código no manejado específicamente")
                }
            }

            Log.d("TaskRepository", "Retornando response desde repositorio")
            response

        } catch (e: Exception) {
            Log.e("TaskRepository", "=== EXCEPCIÓN EN getTareaById REPOSITORIO ===", e)
            Log.e("TaskRepository", "Tipo de excepción: ${e.javaClass.simpleName}")
            Log.e("TaskRepository", "Mensaje de excepción: ${e.message}")
            Log.e("TaskRepository", "Causa: ${e.cause}")

            // Re-lanzar la excepción para que la maneje el ViewModel
            throw e
        }
    }

    override suspend fun crearTarea(
        titulo: String,
        descripcion: String?,
        prioridad: String?,
        estado: String?,
        fecha: String?,
        base64Image: String?
    ): Response<TaskResponse> {
        Log.d("TaskRepository", "=== crearTarea REPOSITORIO ===")
        Log.d("TaskRepository", "Parámetros:")
        Log.d("TaskRepository", "- titulo: '$titulo'")
        Log.d("TaskRepository", "- descripcion: '$descripcion'")
        Log.d("TaskRepository", "- prioridad: '$prioridad'")
        Log.d("TaskRepository", "- estado: '$estado'")
        Log.d("TaskRepository", "- fecha: '$fecha'")
        Log.d("TaskRepository", "- base64Image length: ${base64Image?.length ?: 0}")

        val request = TaskRequest(
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad,
            estado = estado,
            fecha = fecha,
            base64Image = base64Image
        )

        Log.d("TaskRepository", "Request object creado: $request")

        return try {
            Log.d("TaskRepository", "Llamando a taskService.crearTarea...")
            val response = taskService.crearTarea(request)

            Log.d("TaskRepository", "Respuesta de crearTarea:")
            Log.d("TaskRepository", "- Code: ${response.code()}")
            Log.d("TaskRepository", "- IsSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                Log.d("TaskRepository", "Tarea creada exitosamente")
                val tarea = response.body()
                Log.d("TaskRepository", "Tarea creada: ${tarea?._id}")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TaskRepository", "Error al crear tarea: $errorBody")
            }

            response
        } catch (e: Exception) {
            Log.e("TaskRepository", "Excepción en crearTarea", e)
            throw e
        }
    }

    override suspend fun actualizarTarea(
        id: String,
        titulo: String,
        descripcion: String?,
        prioridad: String?,
        estado: String?,
        fecha: String?,
        base64Image: String?,
        eliminarImagen: Boolean?
    ): Response<TaskResponse> {
        Log.d("TaskRepository", "=== actualizarTarea REPOSITORIO ===")
        Log.d("TaskRepository", "Parámetros de actualización:")
        Log.d("TaskRepository", "- id: '$id'")
        Log.d("TaskRepository", "- titulo: '$titulo'")
        Log.d("TaskRepository", "- descripcion: '$descripcion'")
        Log.d("TaskRepository", "- prioridad: '$prioridad'")
        Log.d("TaskRepository", "- estado: '$estado'")
        Log.d("TaskRepository", "- fecha: '$fecha'")
        Log.d("TaskRepository", "- base64Image length: ${base64Image?.length ?: 0}")
        Log.d("TaskRepository", "- eliminarImagen: $eliminarImagen")

        val request = TaskUpdateRequest(
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad,
            estado = estado,
            fecha = fecha,
            base64Image = base64Image,
            eliminarImagen = eliminarImagen
        )

        Log.d("TaskRepository", "Request de actualización creado: $request")

        return try {
            Log.d("TaskRepository", "Llamando a taskService.actualizarTarea...")
            val response = taskService.actualizarTarea(id, request)

            Log.d("TaskRepository", "Respuesta de actualizarTarea:")
            Log.d("TaskRepository", "- Code: ${response.code()}")
            Log.d("TaskRepository", "- IsSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                Log.d("TaskRepository", "Tarea actualizada exitosamente")
                val tarea = response.body()
                Log.d("TaskRepository", "Tarea actualizada: ${tarea?._id}")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TaskRepository", "Error al actualizar tarea: $errorBody")
            }

            response
        } catch (e: Exception) {
            Log.e("TaskRepository", "Excepción en actualizarTarea", e)
            throw e
        }
    }

    override suspend fun eliminarTarea(id: String): Response<Unit> {
        Log.d("TaskRepository", "=== eliminarTarea REPOSITORIO ===")
        Log.d("TaskRepository", "ID a eliminar: '$id'")

        return try {
            Log.d("TaskRepository", "Llamando a taskService.eliminarTarea...")
            val response = taskService.eliminarTarea(id)

            Log.d("TaskRepository", "Respuesta de eliminarTarea:")
            Log.d("TaskRepository", "- Code: ${response.code()}")
            Log.d("TaskRepository", "- IsSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                Log.d("TaskRepository", "Tarea eliminada exitosamente")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TaskRepository", "Error al eliminar tarea: $errorBody")
            }

            response
        } catch (e: Exception) {
            Log.e("TaskRepository", "Excepción en eliminarTarea", e)
            throw e
        }
    }
}