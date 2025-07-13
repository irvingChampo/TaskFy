package com.example.myapplication.src.Features.Register.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.src.Features.Register.di.AppModule
import com.example.myapplication.src.Features.Register.domain.usecase.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase = AppModule.registerUserUseCase
) : ViewModel() {

    // ✅ PRIVADA - Solo el ViewModel puede modificar
    private val _registerState = MutableStateFlow(RegisterUiState())
    // ✅ PÚBLICA - Solo lectura para la UI
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    // ✅ PRIVADA - Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ PRIVADA - Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ✅ PRIVADA - Success state
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun register(nombre: String, correo: String, contraseña: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val response = registerUserUseCase.execute(nombre, correo, contraseña)

                if (response.isSuccessful) {
                    val mensaje = response.body()?.mensaje ?: "Usuario registrado exitosamente"
                    _successMessage.value = mensaje
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Datos inválidos"
                        409 -> "El usuario ya existe"
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

    fun clearSuccess() {
        _successMessage.value = null
    }
}

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)