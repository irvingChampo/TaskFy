package com.example.myapplication.src.Features.Register.domain.repository

import com.example.myapplication.src.Features.Register.data.model.RegisterResponse
import retrofit2.Response

interface RegisterRepository {
    suspend fun registerUser(
        nombre: String,
        correo: String,
        contraseña: String
    ): Response<RegisterResponse>
}