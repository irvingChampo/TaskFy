package com.example.myapplication.src.Features.Login.domain.model

sealed class LoginResult {
    data class Success(val token: String) : LoginResult()
    data class Error(val message: String, val code: Int? = null) : LoginResult()
    data class ValidationError(val errors: List<String>) : LoginResult()
    object Loading : LoginResult()
}