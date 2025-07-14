package com.example.myapplication.src.Core.DI
import com.example.myapplication.src.Core.AppContext.AppContextHolder
import com.example.myapplication.src.Core.Room.AppDatabase
import com.example.myapplication.src.Core.Room.Dao.*
import com.example.myapplication.src.Core.Room.Relations.*

object DatabaseModule {

    val database: AppDatabase by lazy {
        AppDatabase.create(AppContextHolder.get())
    }

    val userDao: UserDao by lazy {
        database.userDao()
    }

    val taskDao: TaskDao by lazy {
        database.taskDao()
    }

    val userWithTasksDao: UserWithTasksDao by lazy {
        database.userWithTasksDao()
    }

    suspend fun getCurrentUserId(): String? {
        return userDao.getCurrentUserId()
    }

    suspend fun getCurrentUserToken(): String? {
        return userDao.getCurrentUserToken()
    }

    suspend fun isUserLoggedIn(): Boolean {
        return userDao.getCurrentUser() != null
    }
}