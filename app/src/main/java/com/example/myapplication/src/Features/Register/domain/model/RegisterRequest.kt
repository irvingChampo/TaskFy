package com.example.myapplication.src.Features.Register.domain.model

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val contraseña: String
) {
    fun isValid(): Boolean {
        return nombre.isNotBlank() &&
                correo.isNotBlank() &&
                correo.contains("@") &&
                contraseña.length >= 6
    }
}