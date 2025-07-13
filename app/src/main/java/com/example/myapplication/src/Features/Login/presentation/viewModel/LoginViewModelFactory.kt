package com.example.myapplication.src.Features.Login.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.src.Features.Login.di.AppModule
import com.example.myapplication.src.Features.Login.domain.usecase.LoginUseCase

class LoginViewModelFactory(
    private val loginUseCase: LoginUseCase = AppModule.loginUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}