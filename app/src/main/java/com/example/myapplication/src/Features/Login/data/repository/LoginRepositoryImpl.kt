package com.example.myapplication.src.Features.Login.data.repository

import com.example.myapplication.src.Features.Login.data.datasource.remote.LoginService
import com.example.myapplication.src.Features.Login.data.model.LoginRequest
import com.example.myapplication.src.Features.Login.data.model.LoginResponse
import com.example.myapplication.src.Features.Login.domain.repository.LoginRepository
import com.example.myapplication.src.Features.Login.domain.repository.TokenRepository
import retrofit2.Response

class LoginRepositoryImpl(
    private val loginService: LoginService,
    private val tokenRepository: TokenRepository
) : LoginRepository {

    override suspend fun login(correo: String, contraseña: String): Response<LoginResponse> {
        val request = LoginRequest(
            correo = correo,
            contraseña = contraseña
        )

        return try {
            val response = loginService.login(request)

            if (response.isSuccessful) {
                response.body()?.token?.let { token ->
                    tokenRepository.saveToken(token)
                }
            }

            response
        } catch (e: Exception) {
            throw e
        }
    }
}