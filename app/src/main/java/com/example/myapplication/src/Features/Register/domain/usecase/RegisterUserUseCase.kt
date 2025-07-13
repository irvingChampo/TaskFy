package com.example.myapplication.src.Features.Register.domain.usecase

import com.example.myapplication.src.Features.Register.data.model.RegisterResponse
import com.example.myapplication.src.Features.Register.domain.repository.RegisterRepository
import retrofit2.Response

class RegisterUserUseCase(
    private val registerRepository: RegisterRepository
) {

    suspend fun execute(
        nombre: String,
        correo: String,
        contraseña: String
    ): Response<RegisterResponse> {

        if (nombre.isBlank() || correo.isBlank() || contraseña.length < 6) {
            throw IllegalArgumentException("Datos inválidos")
        }

        return registerRepository.registerUser(
            nombre = nombre.trim(),
            correo = correo.trim().lowercase(),
            contraseña = contraseña
        )
    }
}