package com.example.myapplication.src.Features.Login.data.repository

import android.util.Log
import com.example.myapplication.src.Core.DI.DatabaseModule
import com.example.myapplication.src.Core.Room.Entities.UserEntity
import com.example.myapplication.src.Features.Login.data.datasource.remote.AuthService
import com.example.myapplication.src.Features.Login.data.datasource.remote.RegisterFCMRequest
import com.example.myapplication.src.Features.Login.data.model.LoginRequest
import com.example.myapplication.src.Features.Login.data.model.LoginResponse
import com.example.myapplication.src.Features.Login.domain.repository.LoginRepository
import com.example.myapplication.src.Features.Login.domain.repository.TokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.util.UUID

class LoginRepositoryImpl(
    private val authService: AuthService,
    private val tokenRepository: TokenRepository
) : LoginRepository {

    private val userDao = DatabaseModule.userDao

    override suspend fun login(correo: String, contraseña: String): Response<LoginResponse> {
        val request = LoginRequest(correo = correo, contraseña = contraseña)

        try {
            val apiResponse = authService.login(request)

            if (apiResponse.isSuccessful) {
                val token = apiResponse.body()?.token
                if (token != null) {
                    tokenRepository.saveToken(token)
                    val cachedUser = userDao.getUserByEmail(correo)
                    manageUserInDatabase(correo, token, cachedUser)

                    try {
                        val fcmToken = FirebaseMessaging.getInstance().token.await()
                        Log.d("LoginRepo", "FCM Token obtenido: $fcmToken")
                        val registerResponse = authService.registerFCMToken(RegisterFCMRequest(fcmToken))
                        if (registerResponse.isSuccessful) {
                            Log.d("LoginRepo", "Token FCM registrado en la API exitosamente.")
                        } else {
                            Log.e("LoginRepo", "Error al registrar token FCM en la API: ${registerResponse.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("LoginRepo", "No se pudo obtener o registrar el token de FCM.", e)
                    }
                }
            }
            return apiResponse

        } catch (e: Exception) {
            Log.e("LoginRepo", "Excepción durante el login. Intentando usar caché.", e)
            val cachedUser = userDao.getUserByEmail(correo)
            if (cachedUser != null) {
                return Response.success(LoginResponse(token = cachedUser.token))
            }
            throw e
        }
    }

    private suspend fun manageUserInDatabase(correo: String, token: String, existingUser: UserEntity?) {
        try {
            userDao.logoutAllUsers()
            val currentTime = System.currentTimeMillis()
            if (existingUser != null) {
                userDao.updateUserToken(
                    userId = existingUser.id,
                    token = token,
                    lastLogin = currentTime
                )
                userDao.setUserAsLoggedIn(existingUser.id)
            } else {
                val newUser = UserEntity(
                    id = UUID.randomUUID().toString(),
                    nombre = extractNameFromEmail(correo),
                    correo = correo,
                    token = token,
                    isLoggedIn = true,
                    lastLogin = currentTime,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                userDao.insertUser(newUser)
            }
        } catch (e: Exception) {
            Log.e("LoginRepo", "Error al gestionar usuario en DB local", e)
        }
    }

    private fun extractNameFromEmail(email: String): String {
        return email.substringBefore("@")
            .replace(".", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
    }
}