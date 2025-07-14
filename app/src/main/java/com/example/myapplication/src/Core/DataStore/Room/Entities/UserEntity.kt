package com.example.myapplication.src.Core.Room.Entities

import androidx.room.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "correo")
    val correo: String,

    @ColumnInfo(name = "token")
    val token: String,

    @ColumnInfo(name = "is_logged_in")
    val isLoggedIn: Boolean = true,

    @ColumnInfo(name = "last_login")
    val lastLogin: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)