package com.example.myapplication.src.Core.Room.Dao

import androidx.room.*
import com.example.myapplication.src.Core.Room.Entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // ============ AUTHENTICATION ============
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE correo = :correo LIMIT 1")
    suspend fun getUserByEmail(correo: String): UserEntity?

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("UPDATE users SET is_logged_in = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE users SET is_logged_in = 1 WHERE id = :userId")
    suspend fun setUserAsLoggedIn(userId: String)

    @Query("UPDATE users SET token = :token, last_login = :lastLogin WHERE id = :userId")
    suspend fun updateUserToken(userId: String, token: String, lastLogin: Long)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // ============ USER MANAGEMENT ============
    @Query("SELECT * FROM users ORDER BY last_login DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT token FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getCurrentUserToken(): String?

    @Query("SELECT id FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getCurrentUserId(): String?

    @Query("DELETE FROM users WHERE is_logged_in = 0 AND last_login < :beforeDate")
    suspend fun deleteOldLoggedOutUsers(beforeDate: Long)
}