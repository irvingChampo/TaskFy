package com.example.myapplication.src.Core.Room.Relations

import androidx.room.*
import com.example.myapplication.src.Core.Room.Entities.UserEntity
import com.example.myapplication.src.Core.Room.Entities.TaskEntity
import com.example.myapplication.src.Core.Room.Entities.TaskEstado

data class UserWithTasks(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "usuario_id"
    )
    val tasks: List<TaskEntity>
)

data class UserWithTasksSummary(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "usuario_id"
    )
    val tasks: List<TaskEntity>
) {
    val totalTasks: Int get() = tasks.size
    val pendingTasks: Int get() = tasks.count { it.estado == TaskEstado.PENDIENTE }
    val completedTasks: Int get() = tasks.count { it.estado == TaskEstado.COMPLETADA }
    val unsyncedTasks: Int get() = tasks.count { !it.isSynced }
}

@Dao
interface UserWithTasksDao {

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithTasks(userId: String): UserWithTasks?

    @Transaction
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getCurrentUserWithTasks(): UserWithTasks?

    @Transaction
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getCurrentUserWithTasksSummary(): UserWithTasksSummary?
}