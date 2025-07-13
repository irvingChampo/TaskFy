package com.example.myapplication.src.Features.Login.domain.usecase

import com.example.myapplication.src.Features.Login.data.model.LoginResponse
import com.example.myapplication.src.Features.Login.domain.repository.LoginRepository
import retrofit2.Response

class LoginUseCase(
    private val loginRepository: LoginRepository
) {

    suspend fun execute(correo: String, contraseña: String): Response<LoginResponse> {
        if (correo.isBlank() || contraseña.isBlank()) {
            throw IllegalArgumentException("Correo y contraseña son requeridos")
        }

        if (!correo.contains("@")) {
            throw IllegalArgumentException("Formato de correo inválido")
        }

        return loginRepository.login(correo, contraseña)
    }
}