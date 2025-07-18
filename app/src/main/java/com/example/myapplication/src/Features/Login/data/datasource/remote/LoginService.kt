package com.example.myapplication.src.Features.Login.data.datasource.remote

import com.example.myapplication.src.Features.Login.data.model.LoginRequest
import com.example.myapplication.src.Features.Login.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}