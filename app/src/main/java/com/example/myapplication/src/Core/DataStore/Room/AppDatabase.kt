package com.example.myapplication.src.Core.Room

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.myapplication.src.Core.Room.Entities.*
import com.example.myapplication.src.Core.Room.Dao.*
import com.example.myapplication.src.Core.Room.Relations.*
import com.example.myapplication.src.Core.Room.Converters.*

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DateConverters::class,
    TaskConverters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun userWithTasksDao(): UserWithTasksDao

    companion object {
        const val DATABASE_NAME = "task_app_database"
        const val DATABASE_VERSION = 1

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
        }
    }
}