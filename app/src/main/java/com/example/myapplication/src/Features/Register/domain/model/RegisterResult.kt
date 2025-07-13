package com.example.myapplication.src.Features.Register.domain.model

sealed class RegisterResult {
    data class Success(val mensaje: String) : RegisterResult()
    data class Error(val message: String, val code: Int? = null) : RegisterResult()
    data class ValidationError(val errors: List<String>) : RegisterResult()
    object Loading : RegisterResult()
}