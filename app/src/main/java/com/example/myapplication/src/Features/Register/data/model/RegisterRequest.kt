package com.example.myapplication.src.Features.Register.data.model

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val contraseña: String
)