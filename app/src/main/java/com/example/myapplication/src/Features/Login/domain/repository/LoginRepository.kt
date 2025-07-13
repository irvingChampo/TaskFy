package com.example.myapplication.src.Features.Login.domain.repository

import com.example.myapplication.src.Features.Login.data.model.LoginResponse
import retrofit2.Response

interface LoginRepository {
    suspend fun login(correo: String, contraseña: String): Response<LoginResponse>
}