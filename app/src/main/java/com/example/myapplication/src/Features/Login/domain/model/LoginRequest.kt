package com.example.myapplication.src.Features.Login.domain.model

data class LoginRequest(
    val correo: String,
    val contraseña: String
) {
    fun isValid(): Boolean {
        return correo.isNotBlank() &&
                correo.contains("@") &&
                contraseña.isNotBlank()
    }
}