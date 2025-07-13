package com.example.myapplication.src.Features.Register.data.repository

import com.example.myapplication.src.Features.Register.data.datasource.remote.RegisterService
import com.example.myapplication.src.Features.Register.data.model.RegisterRequest
import com.example.myapplication.src.Features.Register.data.model.RegisterResponse
import com.example.myapplication.src.Features.Register.domain.repository.RegisterRepository
import retrofit2.Response

class RegisterRepositoryImpl(
    private val registerService: RegisterService  // ✅ Recibe RegisterService como parámetro
) : RegisterRepository {

    override suspend fun registerUser(
        nombre: String,
        correo: String,
        contraseña: String
    ): Response<RegisterResponse> {
        val request = RegisterRequest(
            nombre = nombre,
            correo = correo,
            contraseña = contraseña
        )

        return registerService.register(request)
    }
}