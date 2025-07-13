package com.example.myapplication.src.Features.Register.di

import com.example.myapplication.src.Core.Network.RetrofitHelper
import com.example.myapplication.src.Features.Register.data.datasource.remote.RegisterService
import com.example.myapplication.src.Features.Register.data.repository.RegisterRepositoryImpl
import com.example.myapplication.src.Features.Register.domain.repository.RegisterRepository
import com.example.myapplication.src.Features.Register.domain.usecase.RegisterUserUseCase

object AppModule {
    init {
        RetrofitHelper.init()
    }

    private val registerService: RegisterService by lazy {
        RetrofitHelper.getService(RegisterService::class.java)
    }

    private val repositoryRegister: RegisterRepository by lazy {
        RegisterRepositoryImpl(registerService)
    }

    val registerUserUseCase: RegisterUserUseCase by lazy {
        RegisterUserUseCase(repositoryRegister) // ✅ Correcto: pasa el repository
    }
}