package com.example.myapplication.src.Features.Login.domain.repository

interface TokenRepository {
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
}