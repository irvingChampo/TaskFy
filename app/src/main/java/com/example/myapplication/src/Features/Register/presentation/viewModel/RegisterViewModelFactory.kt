package com.example.myapplication.src.Features.Register.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.src.Features.Register.di.AppModule
import com.example.myapplication.src.Features.Register.domain.usecase.RegisterUserUseCase

class RegisterViewModelFactory(
    private val registerUserUseCase: RegisterUserUseCase = AppModule.registerUserUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(registerUserUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}