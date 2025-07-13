package com.example.myapplication.src.Features.Login.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.src.Features.Login.di.AppModule
import com.example.myapplication.src.Features.Login.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = AppModule.loginUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoginSuccessful = MutableStateFlow(false)
    val isLoginSuccessful: StateFlow<Boolean> = _isLoginSuccessful.asStateFlow()

    fun login(correo: String, contraseña: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isLoginSuccessful.value = false

            try {
                val response = loginUseCase.execute(correo, contraseña)

                if (response.isSuccessful) {
                    _isLoginSuccessful.value = true
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        404 -> "Usuario no encontrado"
                        500 -> "Error del servidor"
                        else -> "Error desconocido"
                    }
                    _errorMessage.value = errorMessage
                }
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message ?: "Datos inválidos"
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearLoginSuccess() {
        _isLoginSuccessful.value = false
    }
}