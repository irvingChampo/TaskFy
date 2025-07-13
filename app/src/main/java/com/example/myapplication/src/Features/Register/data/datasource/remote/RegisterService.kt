package com.example.myapplication.src.Features.Register.data.datasource.remote

import com.example.myapplication.src.Features.Register.data.model.RegisterRequest
import com.example.myapplication.src.Features.Register.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}