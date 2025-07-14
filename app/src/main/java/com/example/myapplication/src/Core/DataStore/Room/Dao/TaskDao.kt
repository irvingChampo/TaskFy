package com.example.myapplication.src.Core.Room.Dao

import androidx.room.*
import com.example.myapplication.src.Core.Room.Entities.TaskEntity
import com.example.myapplication.src.Core.Room.Entities.TaskEstado
import com.example.myapplication.src.Core.Room.Entities.TaskPrioridad
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT * FROM tasks WHERE usuario_id = :usuarioId ORDER BY updated_at DESC")
    suspend fun getTasksByUser(usuarioId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE usuario_id = :usuarioId ORDER BY updated_at DESC")
    fun getTasksByUserFlow(usuarioId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE usuario_id = :usuarioId AND estado = :estado ORDER BY updated_at DESC")
    suspend fun getTasksByUserAndStatus(usuarioId: String, estado: TaskEstado): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE usuario_id = :usuarioId AND prioridad = :prioridad ORDER BY updated_at DESC")
    suspend fun getTasksByUserAndPriority(usuarioId: String, prioridad: TaskPrioridad): List<TaskEntity>

    @Query("""
        SELECT * FROM tasks 
        WHERE usuario_id = :usuarioId 
        AND (titulo LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%')
        ORDER BY updated_at DESC
    """)
    suspend fun searchTasks(usuarioId: String, query: String): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE usuario_id = :usuarioId")
    suspend fun getTaskCountByUser(usuarioId: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE usuario_id = :usuarioId AND estado = :estado")
    suspend fun getTaskCountByUserAndStatus(usuarioId: String, estado: TaskEstado): Int

    @Query("""
        SELECT estado, COUNT(*) as count 
        FROM tasks 
        WHERE usuario_id = :usuarioId 
        GROUP BY estado
    """)
    suspend fun getTaskStatsByUser(usuarioId: String): List<TaskStatistic>

    @Query("SELECT * FROM tasks WHERE needs_upload = 1")
    suspend fun getTasksNeedingUpload(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE is_synced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET is_synced = 1, needs_upload = 0 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: String)

    @Query("UPDATE tasks SET needs_upload = 1 WHERE id = :taskId")
    suspend fun markTaskForUpload(taskId: String)

    @Query("DELETE FROM tasks WHERE usuario_id = :usuarioId")
    suspend fun deleteAllTasksByUser(usuarioId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM tasks WHERE updated_at < :beforeDate AND is_synced = 1")
    suspend fun deleteOldSyncedTasks(beforeDate: Long)
}

data class TaskStatistic(
    val estado: TaskEstado,
    val count: Int
)