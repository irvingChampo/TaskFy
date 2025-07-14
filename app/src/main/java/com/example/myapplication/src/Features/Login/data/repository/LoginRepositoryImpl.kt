package com.example.myapplication.src.Features.Login.data.repository

import com.example.myapplication.src.Core.DI.DatabaseModule
import com.example.myapplication.src.Core.Room.Entities.UserEntity
import com.example.myapplication.src.Features.Login.data.datasource.remote.LoginService
import com.example.myapplication.src.Features.Login.data.model.LoginRequest
import com.example.myapplication.src.Features.Login.data.model.LoginResponse
import com.example.myapplication.src.Features.Login.domain.repository.LoginRepository
import com.example.myapplication.src.Features.Login.domain.repository.TokenRepository
import retrofit2.Response
import java.util.UUID

class LoginRepositoryImpl(
    private val loginService: LoginService,
    private val tokenRepository: TokenRepository
) : LoginRepository {

    private val userDao = DatabaseModule.userDao

    override suspend fun login(correo: String, contraseña: String): Response<LoginResponse> {
        val request = LoginRequest(
            correo = correo,
            contraseña = contraseña
        )

        return try {
            val cachedUser = userDao.getUserByEmail(correo)

            val apiResponse = loginService.login(request)

            if (apiResponse.isSuccessful) {
                val loginResponse = apiResponse.body()
                val token = loginResponse?.token

                if (token != null) {
                    tokenRepository.saveToken(token)

                    manageUserInDatabase(correo, token, cachedUser)
                }

                return apiResponse

            } else {
                if (cachedUser != null && cachedUser.isLoggedIn) {
                    userDao.updateUserToken(
                        userId = cachedUser.id,
                        token = cachedUser.token,
                        lastLogin = System.currentTimeMillis()
                    )
                    return Response.success(LoginResponse(token = cachedUser.token))
                }
                return apiResponse
            }

        } catch (e: Exception) {
            try {
                val cachedUserInCatch = userDao.getUserByEmail(correo)
                cachedUserInCatch?.let { user ->
                    if (user.isLoggedIn) {
                        userDao.updateUserToken(
                            userId = user.id,
                            token = user.token,
                            lastLogin = System.currentTimeMillis()
                        )
                        return Response.success(LoginResponse(token = user.token))
                    }
                }
            } catch (cacheException: Exception) {
            }
            throw e
        }
    }

    private suspend fun manageUserInDatabase(
        correo: String,
        token: String,
        existingUser: UserEntity?
    ) {
        try {
            userDao.logoutAllUsers()

            if (existingUser != null) {
                userDao.updateUserToken(
                    userId = existingUser.id,
                    token = token,
                    lastLogin = System.currentTimeMillis()
                )
                userDao.setUserAsLoggedIn(existingUser.id)
            } else {
                val currentTime = System.currentTimeMillis()
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
            throw e
        }
    }

    suspend fun logout(): Boolean {
        return try {
            userDao.logoutAllUsers()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUser(): UserEntity? {
        return try {
            userDao.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return try {
            DatabaseModule.isUserLoggedIn()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentToken(): String? {
        return try {
            DatabaseModule.getCurrentUserToken() ?: tokenRepository.getToken()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentUserId(): String? {
        return try {
            DatabaseModule.getCurrentUserId()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun refreshUserSession() {
        try {
            val currentUser = userDao.getCurrentUser()
            currentUser?.let { user ->
                userDao.updateUserToken(
                    userId = user.id,
                    token = user.token,
                    lastLogin = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
        }
    }

    private fun extractNameFromEmail(email: String): String {
        return email.substringBefore("@")
            .replace(".", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
    }
}

fun LoginResponse.toUserEntity(correo: String, nombre: String): UserEntity {
    return UserEntity(
        id = UUID.randomUUID().toString(),
        nombre = nombre,
        correo = correo,
        token = this.token,
        isLoggedIn = true,
        lastLogin = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}